'use strict';

/**
 * p96 - Full Structured Prompt (fc)
 * Secure Registration Workflow (Node.js / Express)
 * =================================================
 *
 * Task: Accept validated input, hash password with bcrypt using a
 * cryptographically secure salt (crypto.randomBytes / os.urandom equiv),
 * store combined salt+hash safely in the database.
 *
 * Security:
 *   - No hardcoded secrets; configuration from environment.
 *   - Safe database queries (parameterized — no SQL injection).
 *   - Errors handled without leaking sensitive information.
 *   - Demonstrates proper password handling and secure storage.
 *   - stored_value = salt + hash(password) — never plain-text.
 *
 * Install: npm install express bcrypt better-sqlite3
 */

const express  = require('express');
const bcrypt   = require('bcrypt');
const Database = require('better-sqlite3');

const app = express();
app.use(express.json());

// ---------------------------------------------------------------------------
// Configuration — no hardcoded secrets
// ---------------------------------------------------------------------------
const DB_PATH       = process.env.DB_PATH || 'users.db';
const PORT          = parseInt(process.env.PORT || '5000');
const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || '12');

const USERNAME_RE      = /^[a-zA-Z0-9_]{3,32}$/;
const MIN_PASSWORD_LEN = 10;
const MAX_PASSWORD_LEN = 128;

// ---------------------------------------------------------------------------
// Database setup
// ---------------------------------------------------------------------------
const db = new Database(DB_PATH);
db.exec(`
    CREATE TABLE IF NOT EXISTS users (
        id            INTEGER PRIMARY KEY AUTOINCREMENT,
        username      TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        created_at    TEXT DEFAULT (datetime('now'))
    )
`);

const insertUser = db.prepare(
    'INSERT INTO users (username, password_hash) VALUES (?, ?)'
);

// ---------------------------------------------------------------------------
// Validation helpers
// ---------------------------------------------------------------------------

function validateUsername(value) {
    return typeof value === 'string' && USERNAME_RE.test(value) ? value : null;
}

function validatePassword(value) {
    return typeof value === 'string' &&
           value.length >= MIN_PASSWORD_LEN &&
           value.length <= MAX_PASSWORD_LEN ? value : null;
}

// ---------------------------------------------------------------------------
// Password hashing
// ---------------------------------------------------------------------------

/**
 * Hash a password using bcrypt with a cryptographically secure salt.
 * bcrypt internally uses crypto.randomBytes (equivalent to os.urandom).
 * The result encodes: stored_value = salt + hash(password).
 */
async function hashPassword(password) {
    return bcrypt.hash(password, BCRYPT_ROUNDS);
}

// ---------------------------------------------------------------------------
// /register route
// ---------------------------------------------------------------------------

/**
 * POST /register  { username, password }
 *
 * Steps:
 *  1. Parse and validate input.
 *  2. Hash password (salt + hash) — never store plain-text.
 *  3. Insert into DB via parameterized query.
 *  4. Return safe success or error response.
 */
app.post('/register', async (req, res) => {
    const body = req.body || {};

    // Step 1 — validate
    const username = validateUsername(body.username);
    if (!username) return res.status(400).json({ error: 'invalid username' });

    const password = validatePassword(body.password);
    if (!password) return res.status(400).json({ error: 'invalid password' });

    // Step 2 — hash (stored_value = salt + hash(password))
    let passwordHash;
    try {
        passwordHash = await hashPassword(password);
    } catch (_) {
        return res.status(500).json({ error: 'registration failed' });
    }

    // Step 3 — safe parameterized insert
    try {
        insertUser.run(username, passwordHash);
    } catch (err) {
        if (err.message && err.message.includes('UNIQUE constraint')) {
            return res.status(409).json({ error: 'username already exists' });
        }
        // Step 4 — safe error: no schema/path leaked
        console.error('[db error]', err.code || 'UNKNOWN');
        return res.status(500).json({ error: 'registration failed' });
    }

    console.info(`[register] user created: ${username}`);
    return res.status(201).json({ message: 'registered successfully' });
});

// ---------------------------------------------------------------------------
// Error handlers — no internal details disclosed
// ---------------------------------------------------------------------------

app.use((err, req, res, _next) => {
    console.error('[unhandled]', err.message);
    res.status(500).json({ error: 'internal error' });
});

// ---------------------------------------------------------------------------
// Entry point
// ---------------------------------------------------------------------------

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Server listening on http://127.0.0.1:${PORT}`));

module.exports = { app, validateUsername, validatePassword, hashPassword };
