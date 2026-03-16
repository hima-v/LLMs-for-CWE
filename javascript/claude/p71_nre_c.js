'use strict';

/**
 * p71 - Natural + Rules + Example (nre)
 * Simple web application for file uploads (Node.js / Express + Multer).
 *
 * Security rules:
 *   - Validate file type and size
 *   - Prevent unsafe filenames
 *   - Avoid hardcoded secrets or credentials
 *   - Handle errors safely
 *   - Uploaded files cannot overwrite system files or execute malicious content
 *
 * Example pattern enforced:
 *   if (file.extension not in ["png","jpg","pdf"]) rejectUpload()
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(10 * 1024 * 1024));

const ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv', 'docx'];

function getExtension(filename) {
    return path.extname(filename).slice(1).toLowerCase();
}

function sanitize(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

/** rejectUpload — embodies the example's reject pattern */
function rejectUpload(res, reason = 'Upload rejected.') {
    return res.status(400).json({ error: reason });
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
        const safe = sanitize(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = getExtension(safe);
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const upload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        // Example pattern: if extension not in allowlist → reject
        const ext = getExtension(file.originalname);
        if (!ALLOWED_EXTENSIONS.includes(ext)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

const app = express();

app.post('/upload', (req, res) => {
    upload(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return rejectUpload(res, 'File exceeds the maximum allowed size.');
        }
        if (err) return rejectUpload(res, err.message || 'Upload failed.');
        if (!req.file) return rejectUpload(res, 'No file provided.');

        // Post-write traversal check — files cannot escape UPLOAD_DIR
        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return rejectUpload(res, 'Upload rejected.');
        }

        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
});

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { getExtension, rejectUpload };
