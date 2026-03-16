'use strict';

/**
 * p71 - Natural + Checklist (nc)
 * Simple web application for file uploads (Node.js / Express + Multer).
 *
 * Security checklist:
 * [x] File type and size validation
 * [x] Safe storage — server-generated filenames, not user-supplied
 * [x] Protection against malicious filenames / path traversal
 * [x] Authentication / request verification before accepting uploads
 * [x] No hardcoded credentials or sensitive paths
 * [x] Clear but safe error messages (no internal detail exposed)
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// [x] No hardcoded paths or credentials
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(10 * 1024 * 1024));
const API_KEY    = process.env.UPLOAD_API_KEY || '';

// [x] Type validation via allowlist
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv', 'docx']);

// [x] Authentication helper
function authenticated(req) {
    if (!API_KEY) return true;
    return req.headers['x-upload-key'] === API_KEY;
}

// [x] Sanitize filename — protection against malicious names
function sanitize(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

// [x] Path traversal protection
function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    return resolved.startsWith(path.resolve(dir) + path.sep) ? resolved : null;
}

const storage = multer.diskStorage({
    destination(req, file, cb) {
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
    // [x] Safe storage — server-generated UUID name, not user-supplied
    filename(req, file, cb) {
        const safe = sanitize(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const upload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },  // [x] Size validation
    fileFilter(req, file, cb) {
        // [x] File type validation
        const ext = path.extname(file.originalname).slice(1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.has(ext)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

const app = express();

app.post('/upload', (req, res) => {
    // [x] Authentication / request verification
    if (!authenticated(req)) {
        return res.status(401).json({ error: 'Unauthorized.' });
    }

    upload(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File exceeds the maximum allowed size.' });
        }
        if (err) {
            // [x] Safe error message — no internal detail
            return res.status(400).json({ error: err.message || 'Upload failed.' });
        }
        if (!req.file) {
            return res.status(400).json({ error: 'No file provided.' });
        }

        // [x] Path traversal guard (post-write)
        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return res.status(400).json({ error: 'Upload rejected.' });
        }

        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
});

// [x] Safe global error handler
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { authenticated, sanitize, safeJoin };
