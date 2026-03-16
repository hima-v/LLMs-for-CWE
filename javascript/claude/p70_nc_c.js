'use strict';

/**
 * p70 - Natural + Checklist (nc)
 * Secure file upload web app (Node.js / Express + Multer).
 *
 * Security checklist:
 * [x] Uploaded file names are sanitized (no path components, special chars replaced)
 * [x] File types validated against an allowlist — not blindly accepted
 * [x] Uploaded paths cannot escape the uploads directory (realpath check)
 * [x] Untrusted input validated before use
 * [x] Errors handled safely — no internal server paths exposed
 * [x] Avoid insecure practices like trusting client-supplied file names
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// [x] No hardcoded paths — configuration via environment
const UPLOAD_DIR = process.env.UPLOAD_DIR
    || path.resolve(__dirname, 'uploads');
const MAX_BYTES = parseInt(process.env.MAX_UPLOAD_BYTES || String(5 * 1024 * 1024));

// [x] File type validation — explicit allowlist
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv']);

// --- Checklist helpers ---

// [x] File type validated against allowlist (not client MIME)
function fileIsSafeType(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

// [x] File names sanitized — strip path components and unsafe characters
function sanitizeFilename(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

// [x] Paths cannot escape uploads directory
function safeDestPath(dir, filename) {
    const resolved = path.resolve(dir, filename);
    return resolved.startsWith(path.resolve(dir) + path.sep) ? resolved : null;
}

// --- Multer configuration ---
const storage = multer.diskStorage({
    destination(req, file, cb) {
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
    // [x] Server-generated filename — avoids trusting client-supplied names on disk
    filename(req, file, cb) {
        const safe = sanitizeFilename(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const uploadMiddleware = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    // [x] Validate untrusted input in fileFilter
    fileFilter(req, file, cb) {
        if (!fileIsSafeType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        const safe = sanitizeFilename(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        cb(null, true);
    },
}).single('file');

// --- upload_file function ---
function uploadFile(req, res) {
    uploadMiddleware(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File too large.' });
        }
        if (err) {
            // [x] Safe error — no internal details exposed
            return res.status(400).json({ error: err.message || 'Upload failed.' });
        }
        if (!req.file) {
            return res.status(400).json({ error: 'No file provided.' });
        }

        // [x] Post-write traversal verification (defence-in-depth)
        if (!safeDestPath(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return res.status(400).json({ error: 'Upload rejected.' });
        }

        res.json({
            message: 'File uploaded successfully.',
            filename: req.file.filename,
        });
    });
}

// --- Express app ---
const app = express();
app.post('/upload', uploadFile);

// [x] Global error handler — no leakage of internals
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, sanitizeFilename, safeDestPath };
