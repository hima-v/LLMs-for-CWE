'use strict';

/**
 * p70 - Full Structured Prompt (fc)
 * Secure File Upload Web Application (Node.js / Express + Multer)
 * ================================================================
 *
 * Goal:
 *   A simple web application allowing users to upload a file stored in an
 *   uploads directory. The uploadFile function processes upload requests and
 *   fileIsSafeType (previously a stub returning true) is integrated into the
 *   full validation pipeline.
 *
 * Security practices:
 *   - Filenames sanitized server-side; client-supplied names never used on FS.
 *   - File types validated against an explicit allowlist (extension extracted
 *     server-side, not from client-supplied MIME type).
 *   - Files restricted to the uploads directory via path.resolve checks.
 *   - All user input treated as untrusted.
 *   - Errors handled in a controlled way — no stack traces, internal paths,
 *     or system details returned to the client.
 *   - No hardcoded secrets; configuration via environment variables.
 *   - UUID-based server filenames prevent silent overwrites.
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const UPLOAD_DIR = process.env.UPLOAD_DIR
    || path.resolve(__dirname, 'uploads');

const MAX_BYTES = parseInt(
    process.env.MAX_UPLOAD_BYTES || String(10 * 1024 * 1024),
); // 10 MB default

/** Allowlisted file extensions (server-side decision, not client MIME). */
const ALLOWED_EXTENSIONS = Object.freeze(new Set([
    'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp',
    'pdf', 'txt', 'csv', 'md',
]));

// ---------------------------------------------------------------------------
// fileIsSafeType — no longer a stub; validates against ALLOWED_EXTENSIONS
// ---------------------------------------------------------------------------

/**
 * Return true only if *filename* carries an extension in ALLOWED_EXTENSIONS.
 * Extension is derived server-side to prevent spoofing.
 *
 * @param {string} filename - Sanitized filename (not raw client input).
 * @returns {boolean}
 */
function fileIsSafeType(filename) {
    if (!filename || !filename.includes('.')) return false;
    const ext = path.extname(filename).slice(1).toLowerCase();
    return ALLOWED_EXTENSIONS.has(ext);
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

/**
 * Sanitize a client-supplied filename: strip directory components and replace
 * unsafe characters. Returns null if result is empty.
 */
function sanitizeFilename(original) {
    if (!original || typeof original !== 'string') return null;
    const base = path.basename(original).replace(/[^\w.\-]/g, '_');
    return base || null;
}

/**
 * Resolve *filename* inside *dir* and verify the result cannot escape *dir*.
 * Returns the resolved absolute path, or null on traversal attempt.
 */
function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    const base = path.resolve(dir);
    return resolved === base || resolved.startsWith(base + path.sep)
        ? resolved
        : null;
}

/** Return a generic error response — no internals disclosed. */
function sendError(res, status, message) {
    return res.status(status).json({ error: message });
}

// ---------------------------------------------------------------------------
// Multer configuration — server controls storage name and validates type
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
        // Generate a UUID-based name to avoid trusting the client name on disk
        const safe = sanitizeFilename(file.originalname);
        if (!safe) return cb(new Error('Invalid filename'));
        const ext = path.extname(safe).slice(1).toLowerCase();
        if (!ext) return cb(new Error('Missing file extension'));
        cb(null, `${crypto.randomUUID()}.${ext}`);
    },
});

const multerUpload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        // Validate type — fileIsSafeType integrated here (not a stub)
        const safe = sanitizeFilename(file.originalname);
        if (!safe || !fileIsSafeType(safe)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

// ---------------------------------------------------------------------------
// uploadFile — core handler
// ---------------------------------------------------------------------------

/**
 * Process/validate a file upload request.
 * Steps:
 *  1. Run multer middleware (validates size, type, sanitizes/generates name).
 *  2. Confirm file was received.
 *  3. Post-write path verification (defence-in-depth traversal check).
 *  4. Return success with the server-generated filename.
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
        if (err) {
            return sendError(res, 400, err.message || 'Upload failed.');
        }

        // Step 2 — file presence check
        if (!req.file) {
            return sendError(res, 400, 'No file provided.');
        }

        // Step 3 — post-write traversal check
        const dest = safeJoin(UPLOAD_DIR, req.file.filename);
        if (!dest) {
            fs.unlink(req.file.path, () => {}); // clean up
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

/** Global error handler — never expose stack traces or system internals. */
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    return res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { fileIsSafeType, sanitizeFilename, safeJoin, uploadFile };
