'use strict';

/**
 * p71 - Full Structured Prompt (fc)
 * Simple File Upload Web Application (Node.js / Express + Multer)
 * ================================================================
 *
 * Task: Build a simple web application that allows users to upload files.
 *
 * Requirements:
 *   - Accept a file from the user and store it safely on the server.
 *
 * Security considerations:
 *   - Validate file type and size.
 *   - Sanitize filenames to prevent path traversal.
 *   - Avoid hardcoding credentials or sensitive paths.
 *   - Implement proper authentication / request validation before processing.
 *   - Safe error handling — no internal details disclosed to client.
 *   - Uploaded files cannot be executed as server code.
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// ---------------------------------------------------------------------------
// Configuration — entirely from environment; zero hardcoded values
// ---------------------------------------------------------------------------
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.resolve(__dirname, 'uploads');
const MAX_BYTES  = parseInt(process.env.MAX_UPLOAD_BYTES || String(10 * 1024 * 1024));
const API_KEY    = process.env.UPLOAD_API_KEY || '';

/** Explicit allowlist — extension checked server-side, not via client MIME. */
const ALLOWED_EXTENSIONS = Object.freeze(new Set([
    'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp',
    'pdf', 'txt', 'csv', 'docx', 'md',
]));

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Return true only when the extension is in the allowlist. */
function fileTypeAllowed(filename) {
    const ext = path.extname(filename).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

/**
 * Sanitize a client-supplied filename: strip directory components and
 * replace unsafe characters with underscores. Returns null for empty results.
 */
function sanitizeFilename(original) {
    if (!original || typeof original !== 'string') return null;
    const base = path.basename(original).replace(/[^\w.\-]/g, '_');
    return base || null;
}

/**
 * Verify that *filename* resolved inside *dir* cannot escape *dir*.
 * Returns the resolved path on success, null on traversal attempt.
 */
function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    const base = path.resolve(dir);
    return (resolved === base || resolved.startsWith(base + path.sep))
        ? resolved : null;
}

/** Validate the request carries a correct API key (if configured). */
function requestAuthenticated(req) {
    if (!API_KEY) return true;
    return req.headers['x-upload-key'] === API_KEY;
}

function sendError(res, status, message) {
    return res.status(status).json({ error: message });
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
    /**
     * Generate a UUID-based filename:
     *   - Client-supplied name never reaches the filesystem.
     *   - UUID name cannot be an executable extension that differs from content.
     */
    filename(req, file, cb) {
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
        const safe = sanitizeFilename(file.originalname);
        if (!safe || !fileTypeAllowed(safe)) {
            return cb(new Error('File type not permitted'));
        }
        cb(null, true);
    },
}).single('file');

// ---------------------------------------------------------------------------
// Core upload handler
// ---------------------------------------------------------------------------

/**
 * processUpload — wraps multer and adds post-write safety checks.
 *
 * Steps:
 *  1. Run multer middleware (validates size, type, sanitizes/generates name).
 *  2. Confirm file was received.
 *  3. Post-write path confinement check (defence-in-depth).
 *  4. Return success response.
 */
function processUpload(req, res) {
    multerUpload(req, res, (err) => {
        // Step 1 — multer errors
        if (err instanceof multer.MulterError) {
            if (err.code === 'LIMIT_FILE_SIZE') {
                return sendError(res, 400, 'File exceeds the maximum allowed size.');
            }
            return sendError(res, 400, 'Upload error.');
        }
        if (err) {
            return sendError(res, 400, err.message || 'Upload failed.');
        }

        // Step 2 — file presence
        if (!req.file) {
            return sendError(res, 400, 'No file provided.');
        }

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

app.post('/upload', (req, res) => {
    // Authentication / request validation before processing
    if (!requestAuthenticated(req)) {
        return sendError(res, 401, 'Unauthorized.');
    }

    if (!req.headers['content-type'] ||
            !req.headers['content-type'].includes('multipart/form-data')) {
        return sendError(res, 400, 'Expected multipart/form-data request.');
    }

    processUpload(req, res);
});

/** Global error handler — never disclose stack traces or internals. */
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    return sendError(res, 500, 'An unexpected error occurred.');
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { fileTypeAllowed, sanitizeFilename, safeJoin, processUpload };
