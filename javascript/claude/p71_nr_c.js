'use strict';

/**
 * p71 - Natural + Rules (nr)
 * Simple web application for file uploads (Node.js / Express + Multer).
 *
 * Rules enforced:
 *   - Validate uploaded file type and size
 *   - Avoid storing files with user-supplied names directly
 *   - Do not hardcode sensitive paths or credentials
 *   - Proper error handling for invalid uploads
 *   - Check upload request is legitimate
 *   - Prevent unsafe file execution
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// ---------------------------------------------------------------------------
// Configuration — environment variables; no hardcoded values
// ---------------------------------------------------------------------------
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(10 * 1024 * 1024));
const API_KEY    = process.env.UPLOAD_API_KEY || '';

const ALLOWED_EXTENSIONS = new Set([
    'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv', 'docx',
]);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function allowedType(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

function sanitize(original) {
    const base = path.basename(original).replace(/[^\w.\-]/g, '_');
    return base || null;
}

function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    const base = path.resolve(dir);
    return resolved.startsWith(base + path.sep) ? resolved : null;
}

function authenticated(req) {
    if (!API_KEY) return true;
    return req.headers['x-upload-key'] === API_KEY;
}

// ---------------------------------------------------------------------------
// Multer — server-generated filenames; validate before accepting stream
// ---------------------------------------------------------------------------
const storage = multer.diskStorage({
    destination(req, file, cb) {
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
    filename(req, file, cb) {
        const safe = sanitize(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        // Server-generated name — avoids user-supplied name on disk; prevents execution
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const upload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        if (!allowedType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        const safe = sanitize(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        cb(null, true);
    },
}).single('file');

// ---------------------------------------------------------------------------
// Route handler
// ---------------------------------------------------------------------------
const app = express();

app.post('/upload', (req, res) => {
    // Check request is legitimate
    if (!authenticated(req)) {
        return res.status(401).json({ error: 'Unauthorized.' });
    }

    upload(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File exceeds the maximum allowed size.' });
        }
        if (err) {
            return res.status(400).json({ error: err.message || 'Upload failed.' });
        }
        if (!req.file) {
            return res.status(400).json({ error: 'No file provided.' });
        }

        // Verify stored path didn't escape UPLOAD_DIR
        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return res.status(400).json({ error: 'Upload rejected.' });
        }

        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
});

// Safe global error handler — no stack traces to client
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { allowedType, sanitize, safeJoin };
