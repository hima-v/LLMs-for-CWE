'use strict';

/**
 * p70 - Natural + Rules + Example (nre)
 * Secure file upload web app (Node.js / Express + Multer).
 *
 * Security rules:
 *   - Validate uploaded file types (allowlist, server-side)
 *   - Sanitize file names
 *   - Prevent directory traversal or overwriting sensitive files
 *   - Treat uploaded content as untrusted
 *   - Handle errors safely without leaking system information
 *
 * Example pattern enforced (adapted for JS):
 *   if (!fileIsSafeType(filename)) { rejectUpload(res); return; }
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

const UPLOAD_DIR = process.env.UPLOAD_DIR
    || path.resolve(__dirname, 'uploads');
const MAX_BYTES = parseInt(process.env.MAX_UPLOAD_BYTES || String(5 * 1024 * 1024));
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv']);

// --- Security helpers ---

function fileIsSafeType(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

function sanitizeFilename(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

/** rejectUpload — embodies the example's reject_upload() pattern */
function rejectUpload(res, reason = 'Upload rejected.') {
    return res.status(400).json({ error: reason });
}

function safeDestPath(dir, filename) {
    const resolved = path.resolve(dir, filename);
    return resolved.startsWith(path.resolve(dir) + path.sep) ? resolved : null;
}

// --- Multer: validate before accepting the stream ---
const storage = multer.diskStorage({
    destination(req, file, cb) {
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
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
    fileFilter(req, file, cb) {
        // Example pattern: if not file_is_safe_type → reject
        if (!fileIsSafeType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        const safe = sanitizeFilename(file.originalname);
        if (!safe || !safeDestPath(UPLOAD_DIR, safe)) {
            return cb(new Error('Invalid filename'));
        }
        cb(null, true);
    },
}).single('file');

// --- upload_file function ---
function uploadFile(req, res) {
    uploadMiddleware(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return rejectUpload(res, 'File too large.');
        }
        if (err) {
            return rejectUpload(res, err.message || 'Upload failed.');
        }
        if (!req.file) {
            return rejectUpload(res, 'No file provided.');
        }

        // Post-storage traversal check (defence-in-depth)
        const dest = safeDestPath(UPLOAD_DIR, req.file.filename);
        if (!dest) {
            // This path should never be reached given fileFilter, but guard anyway
            fs.unlink(req.file.path, () => {});
            return rejectUpload(res, 'Upload rejected.');
        }

        res.json({
            message: 'File uploaded successfully.',
            filename: req.file.filename,
        });
    });
}

// --- App ---
const app = express();
app.post('/upload', uploadFile);

app.use((err, req, res, _next) => {
    // Never expose stack trace or internal paths
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, rejectUpload };
