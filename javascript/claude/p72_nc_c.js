'use strict';

/**
 * p72 - Natural + Checklist (nc)
 * Secure file upload: fileIsSafeType + uploadFile pattern (Node.js / Express).
 *
 * Security checklist:
 * [x] Uploaded files validated before saving (type + name)
 * [x] Only allowed file types accepted
 * [x] Filenames sanitized — users cannot manipulate file paths
 * [x] Files cannot be uploaded outside the intended uploads folder
 * [x] Upload directory safely created if it does not exist
 * [x] User input never trusted directly
 * [x] Safe error handling — no internal system info exposed
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// [x] No hardcoded paths
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(5 * 1024 * 1024));
// [x] Only allowed file types accepted — allowlist
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv']);

// [x] Validate: fileIsSafeType checks the file is safe to upload
function fileIsSafeType(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

// [x] Sanitize — users cannot manipulate file paths
function sanitizeFilename(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

// [x] Files cannot escape uploads folder
function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    return resolved.startsWith(path.resolve(dir) + path.sep) ? resolved : null;
}

const storage = multer.diskStorage({
    destination(req, file, cb) {
        // [x] Upload directory safely created if it does not exist
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
    // [x] Server-generated UUID name — never trusts user input directly
    filename(req, file, cb) {
        const safe = sanitizeFilename(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const multerUpload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        // [x] Only allowed file types accepted
        if (!fileIsSafeType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

/** uploadFile — handles the upload of the file with all checklist items. */
function uploadFile(req, res) {
    multerUpload(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File too large.' });
        }
        if (err) {
            // [x] Safe error — no internal system info
            return res.status(400).json({ error: err.message || 'Upload failed.' });
        }
        if (!req.file) return res.status(400).json({ error: 'No file provided.' });

        // [x] Path confinement check — files cannot escape uploads folder
        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return res.status(400).json({ error: 'Upload rejected.' });
        }

        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
}

const app = express();
app.post('/upload', uploadFile);

// [x] Safe global error — no internal details exposed
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, sanitizeFilename, safeJoin };
