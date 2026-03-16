'use strict';

/**
 * p70 - Natural + Rules (nr)
 * Secure file upload web app (Node.js / Express + Multer).
 *
 * Rules enforced:
 *   - Validate file names and file types
 *   - Prevent directory traversal or arbitrary file writes
 *   - Avoid hardcoded paths or secrets
 *   - Handle errors safely without exposing internal details
 *   - Treat all user input as untrusted
 */

const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const express = require('express');
const multer = require('multer');

// ---------------------------------------------------------------------------
// Configuration — no hardcoded paths; use env vars
// ---------------------------------------------------------------------------
const UPLOAD_DIR = process.env.UPLOAD_DIR
    || path.resolve(__dirname, 'uploads');
const MAX_BYTES = parseInt(process.env.MAX_UPLOAD_BYTES || String(5 * 1024 * 1024));
const ALLOWED_EXTENSIONS = new Set(['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv']);

// ---------------------------------------------------------------------------
// Security helpers
// ---------------------------------------------------------------------------

function fileIsSafeType(originalName) {
    const ext = path.extname(originalName).slice(1).toLowerCase();
    return Boolean(ext) && ALLOWED_EXTENSIONS.has(ext);
}

function sanitizeFilename(original) {
    // Strip any directory components and non-word characters
    const base = path.basename(original).replace(/[^\w.\-]/g, '_');
    return base || null;
}

function safeJoin(dir, filename) {
    const resolved = path.resolve(dir, filename);
    return resolved.startsWith(path.resolve(dir) + path.sep) ? resolved : null;
}

// ---------------------------------------------------------------------------
// Multer storage — server-generated filename to avoid trust issues
// ---------------------------------------------------------------------------
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

const upload = multer({
    storage,
    limits: { fileSize: MAX_BYTES },
    fileFilter(req, file, cb) {
        // Validate type against allowlist — treat client MIME as untrusted
        if (!fileIsSafeType(file.originalname)) {
            return cb(new Error('File type not permitted'));
        }
        // Directory-traversal guard on original name
        const safe = sanitizeFilename(file.originalname);
        if (!safe || !safeJoin(UPLOAD_DIR, safe)) {
            return cb(new Error('Invalid filename'));
        }
        cb(null, true);
    },
});

// ---------------------------------------------------------------------------
// upload_file handler
// ---------------------------------------------------------------------------
function uploadFile(req, res) {
    upload.single('file')(req, res, (err) => {
        if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({ error: 'File too large.' });
        }
        if (err) {
            // Generic message — no internal detail exposed
            return res.status(400).json({ error: err.message || 'Upload failed.' });
        }
        if (!req.file) {
            return res.status(400).json({ error: 'No file provided.' });
        }
        res.json({ message: 'File uploaded successfully.', filename: req.file.filename });
    });
}

// ---------------------------------------------------------------------------
// App
// ---------------------------------------------------------------------------
const app = express();

app.post('/upload', uploadFile);

// Safe error handler — never expose stack traces
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'An unexpected error occurred.' });
});

const PORT = parseInt(process.env.PORT || '3000');
app.listen(PORT, '127.0.0.1', () => {
    console.log(`Server listening on http://127.0.0.1:${PORT}`);
});

module.exports = { fileIsSafeType, sanitizeFilename, safeJoin };
