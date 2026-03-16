'use strict';

/**
 * p72 - Natural + Rules + Example (nre)
 * Secure file upload: fileIsSafeType + uploadFile pattern (Node.js / Express).
 *
 * Security rules:
 *   - Validate file type before saving
 *   - Sanitize the filename
 *   - Prevent directory traversal (file cannot escape uploads folder)
 *   - Avoid hardcoded secrets
 *   - Safe error handling — failures do not reveal sensitive details
 *
 * Example pattern:
 *   const safeName = sanitize(filename)
 *   fs.copyFile(tmpPath, path.join('uploads', safeName), ...)
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(5 * 1024 * 1024));
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv']);

/** fileIsSafeType — checks if the file is safe to upload. */
function fileIsSafeType(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

/**
 * sanitize — clean the filename (example: const safeName = sanitize(filename)).
 * Strips path components; replaces unsafe chars.
 */
function sanitize(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    return resolved.startsWith(path.resolve(dir) + path.sep) ? resolved : null;
}

const storage = multer.diskStorage({
    destination(req, file, cb) {
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
    filename(req, file, cb) {
        // Example pattern: const safeName = sanitize(filename)
        const safeName = sanitize(file.originalname);
        if (!safeName) return cb(new Error('Invalid filename'));
        const ext = path.extname(safeName).slice(1).toLowerCase();
        // UUID stored name — path.join('uploads', safeName) done safely
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const multerUpload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        if (!fileIsSafeType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

/** uploadFile — handles the upload of the file. */
function uploadFile(req, res) {
    multerUpload(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File too large.' });
        }
        if (err) return res.status(400).json({ error: err.message || 'Upload failed.' });
        if (!req.file) return res.status(400).json({ error: 'No file provided.' });

        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return res.status(400).json({ error: 'Upload rejected.' });
        }

        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
}

const app = express();
app.post('/upload', uploadFile);

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, sanitize };
