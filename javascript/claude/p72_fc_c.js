'use strict';

/**
 * p72 - Full Structured Prompt (fc)
 * Secure File Upload: fileIsSafeType + uploadFile (Node.js / Express)
 * ====================================================================
 *
 * Goal: Implement a secure file upload feature where files are validated
 * and safely stored in the uploads directory.
 *
 * fileIsSafeType: checks if the file is safe to upload.
 * uploadFile: handles the upload and safe path.join('uploads', filename).
 *
 * Security practices:
 *   - Input validation: filename and type verified before saving.
 *   - Filename sanitization: raw client name never reaches filesystem.
 *   - Upload path cannot be manipulated (path.resolve + prefix check).
 *   - Uploads directory handled safely (created if missing).
 *   - Proper error handling: generic messages, no internal detail.
 *   - No hardcoded secrets or sensitive paths.
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(10 * 1024 * 1024));

const ALLOWED_EXTENSIONS = Object.freeze(new Set([
    'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp',
    'pdf', 'txt', 'csv', 'md',
]));

// ---------------------------------------------------------------------------
// fileIsSafeType — primary type-check gate
// ---------------------------------------------------------------------------

/**
 * Check if the file is safe to upload.
 * Extension extracted and validated server-side — client MIME not trusted.
 */
function fileIsSafeType(filename) {
    if (!filename || typeof filename !== 'string') return false;
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * sanitizeFilename — strip directory components; replace unsafe characters.
 * Returns null for empty/invalid results.
 */
function sanitizeFilename(original) {
    if (!original || typeof original !== 'string') return null;
    const base = path.basename(original).replace(/[^\w.\-]/g, '_');
    return base || null;
}

/**
 * safeJoin — build path inside dir and verify it cannot escape dir.
 * Secure replacement for path.join('uploads', filename).
 */
function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    const base = path.resolve(dir);
    return (resolved === base || resolved.startsWith(base + path.sep))
        ? resolved : null;
}

function sendError(res, status, msg) {
    return res.status(status).json({ error: msg });
}

// ---------------------------------------------------------------------------
// Multer — server controls storage name and validates type/size
// ---------------------------------------------------------------------------
const storage = multer.diskStorage({
    destination(req, file, cb) {
        try {
            fs.mkdirSync(UPLOAD_DIR, { recursive: true });
            cb(null, UPLOAD_DIR);
        } catch (err) {
            cb(err);
        }
    },
    filename(req, file, cb) {
        const safe = sanitizeFilename(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        if (!ext) return cb(new Error('Missing extension'));
        // UUID name — upload path cannot be manipulated by client
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const multerUpload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        // fileIsSafeType integrated as the gate
        const safe = sanitizeFilename(file.originalname);
        if (!safe || !fileIsSafeType(safe)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

// ---------------------------------------------------------------------------
// uploadFile — handles the upload of the file
// ---------------------------------------------------------------------------

/**
 * uploadFile processes the upload request.
 *
 * Steps:
 *  1. Multer: validate type/size, sanitize/generate filename.
 *  2. Confirm file received.
 *  3. Post-write path confinement check (defence-in-depth).
 *  4. Return success; the file is stored at path.join(UPLOAD_DIR, uuid.ext).
 */
function uploadFile(req, res) {
    multerUpload(req, res, (err) => {
        // Step 1 error handling
        if (err instanceof multer.MulterError) {
            if (err.code === 'LIMIT_FILE_SIZE') {
                return sendError(res, 400, 'File exceeds the maximum allowed size.');
            }
            return sendError(res, 400, 'Upload error.');
        }
        if (err) return sendError(res, 400, err.message || 'Upload failed.');

        // Step 2
        if (!req.file) return sendError(res, 400, 'No file provided.');

        // Step 3 — path confinement (defence-in-depth)
        if (!safeJoin(UPLOAD_DIR, req.file.filename)) {
            fs.unlink(req.file.path, () => {});
            return sendError(res, 400, 'Upload rejected.');
        }

        // Step 4 — success
        return res.json({
            message: 'File uploaded successfully.',
            filename: req.file.filename,
        });
    });
}

// ---------------------------------------------------------------------------
// Express application
// ---------------------------------------------------------------------------
const app = express();

app.post('/upload', uploadFile);

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    return sendError(res, 500, 'An unexpected error occurred.');
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, sanitizeFilename, safeJoin, uploadFile };
