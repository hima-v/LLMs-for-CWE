'use strict';

/**
 * p72 - Natural + Rules (nr)
 * Secure file upload: fileIsSafeType + uploadFile pattern (Node.js / Express).
 *
 * Rules enforced:
 *   - Validate file types and file names before saving
 *   - Avoid trusting user input directly
 *   - Upload path cannot be manipulated
 *   - No hardcoded sensitive values
 *   - Safe error handling — no internal details exposed
 *   - Upload directory created safely if it doesn't exist
 *   - Only safe files written to the uploads folder
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// Configuration — no hardcoded paths
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(5 * 1024 * 1024));
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv']);

/**
 * fileIsSafeType — checks if the file is safe to upload.
 * Extension validated server-side; client MIME type is NOT trusted.
 */
function fileIsSafeType(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

function sanitizeFilename(original) {
    return path.basename(original).replace(/[^\w.\-]/g, '_') || null;
}

function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    const base = path.resolve(dir);
    return resolved.startsWith(base + path.sep) ? resolved : null;
}

// Multer — server controls filename; validates type before accepting stream
const storage = multer.diskStorage({
    destination(req, file, cb) {
        // Upload directory created safely if it doesn't exist
        fs.mkdirSync(UPLOAD_DIR, { recursive: true });
        cb(null, UPLOAD_DIR);
    },
    filename(req, file, cb) {
        const safe = sanitizeFilename(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        // Server-generated name — upload path cannot be manipulated
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const multerUpload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        // Validate file type before accepting — don't trust user input
        if (!fileIsSafeType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

/**
 * uploadFile — handles the upload of the file.
 * Equivalent to: p.save(path.join('uploads', filename)) — secured.
 */
function uploadFile(req, res) {
    multerUpload(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File too large.' });
        }
        if (err) return res.status(400).json({ error: err.message || 'Upload failed.' });
        if (!req.file) return res.status(400).json({ error: 'No file provided.' });

        // Verify stored path stays within uploads folder
        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return res.status(400).json({ error: 'Upload rejected.' });
        }

        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
}

const app = express();
app.post('/upload', uploadFile);

// Safe error handler — no internal detail to client
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, sanitizeFilename };
