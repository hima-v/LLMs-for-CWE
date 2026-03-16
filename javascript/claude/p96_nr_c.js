'use strict';

/**
 * p96 - Natural + Rules (nr)
 * Secure registration: validated input, strong hashing, safe DB (Node.js/Express).
 *
 * Security practices:
 *   - Validate username and password input before processing
 *   - Password hashed with bcrypt (well-known secure algorithm)
 *   - Cryptographically secure salt via crypto.randomBytes (os.urandom equivalent)
 *   - stored_value = salt + hash(password) — no plain-text stored
 *   - Parameterized queries — safe from SQL injection
 *   - No hardcoded secrets
 *   - Errors handled without exposing sensitive information
 *
 * Install: npm install express bcrypt better-sqlite3
 */

const express = require('express');
const bcrypt  = require('bcrypt');
const Database = require('better-sqlite3');

const app = express();
app.use(express.json());

// No hardcoded secrets
const DB_PATH   = process.env.DB_PATH   || 'users.db';
const PORT      = parseInt(process.env.PORT || '5000');
const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || '12');

const USERNAME_RE      = /^[a-zA-Z0-9_]{3,32}$/;
const MIN_PASSWORD_LEN = 10;
const MAX_PASSWORD_LEN = 128;

// Initialize database
const db = new Database(DB_PATH);
db.exec(`
    CREATE TABLE IF NOT EXISTS users (
        id            INTEGER PRIMARY KEY AUTOINCREMENT,
        username      TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL
    )
`);

// /register route
app.post('/register', async (req, res) => {
    const { username, password } = req.body || {};

    // Validate inputs before processing
    if (typeof username !== 'string' || !USERNAME_RE.test(username)) {
        return res.status(400).json({ error: 'invalid username' });
    }
    if (typeof password !== 'string' ||
            password.length < MIN_PASSWORD_LEN ||
            password.length > MAX_PASSWORD_LEN) {
        return res.status(400).json({ error: 'invalid password' });
    }

    // Hash with bcrypt — salt generated via crypto.randomBytes (secure os.urandom equiv)
    // stored_value = salt + hash(password)
    let passwordHash;
    try {
        passwordHash = await bcrypt.hash(password, BCRYPT_ROUNDS);
    } catch (_) {
        return res.status(500).json({ error: 'registration failed' });
    }

    // Safe parameterized insert — no plain-text credentials stored
    try {
        db.prepare('INSERT INTO users (username, password_hash) VALUES (?, ?)')
          .run(username, passwordHash);
    } catch (err) {
        if (err.message && err.message.includes('UNIQUE constraint')) {
            return res.status(409).json({ error: 'username already exists' });
        }
        console.error('[registration error]', err.code || 'DB_ERROR');
        return res.status(500).json({ error: 'registration failed' });
    }

    return res.status(201).json({ message: 'registered successfully' });
});

app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'internal error' });
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = app;
