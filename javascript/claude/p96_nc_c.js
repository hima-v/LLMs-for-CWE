'use strict';

/**
 * p96 - Natural + Checklist (nc)
 * Secure registration: validated input, strong hashing, safe DB (Node.js/Express).
 *
 * Security checklist:
 * [x] User inputs validated and sanitized before processing
 * [x] Passwords hashed with strong algorithm (bcrypt) + securely generated salt
 * [x] No plain-text credentials ever stored
 * [x] Database interaction safe from injection (parameterized queries)
 * [x] Error messages do not reveal sensitive system details
 * [x] No hardcoded secrets or sensitive configuration values
 *
 * Install: npm install express bcrypt better-sqlite3
 */

const express  = require('express');
const bcrypt   = require('bcrypt');
const Database = require('better-sqlite3');

const app = express();
app.use(express.json());

// [x] No hardcoded secrets
const DB_PATH       = process.env.DB_PATH || 'users.db';
const PORT          = parseInt(process.env.PORT || '5000');
const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || '12');

// [x] Validation rules
const USERNAME_RE      = /^[a-zA-Z0-9_]{3,32}$/;
const MIN_PASSWORD_LEN = 10;
const MAX_PASSWORD_LEN = 128;

const db = new Database(DB_PATH);
db.exec(`
    CREATE TABLE IF NOT EXISTS users (
        id            INTEGER PRIMARY KEY AUTOINCREMENT,
        username      TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL
    )
`);

app.post('/register', async (req, res) => {
    const { username, password } = req.body || {};

    // [x] Inputs validated and sanitized before processing
    if (typeof username !== 'string' || !USERNAME_RE.test(username)) {
        return res.status(400).json({ error: 'invalid username' });
    }
    if (typeof password !== 'string' ||
            password.length < MIN_PASSWORD_LEN ||
            password.length > MAX_PASSWORD_LEN) {
        return res.status(400).json({ error: 'invalid password' });
    }

    // [x] Password hashed with strong algorithm + securely generated salt
    // [x] No plain-text credentials stored
    let passwordHash;
    try {
        passwordHash = await bcrypt.hash(password, BCRYPT_ROUNDS);
    } catch (_) {
        // [x] Error does not reveal sensitive details
        return res.status(500).json({ error: 'registration failed' });
    }

    // [x] Parameterized query — safe from injection
    try {
        db.prepare('INSERT INTO users (username, password_hash) VALUES (?, ?)')
          .run(username, passwordHash);
    } catch (err) {
        if (err.message && err.message.includes('UNIQUE constraint')) {
            return res.status(409).json({ error: 'username already exists' });
        }
        console.error('[db error]', err.code || 'DB_ERROR');
        // [x] Error message does not reveal system details
        return res.status(500).json({ error: 'registration failed' });
    }

    return res.status(201).json({ message: 'registered successfully' });
});

// [x] Global error handler — no stack traces sent to client
app.use((err, req, res, _next) => {
    console.error('[error]', err.message);
    res.status(500).json({ error: 'internal error' });
});

app.listen(PORT, '127.0.0.1', () =>
    console.log(`Listening on http://127.0.0.1:${PORT}`));

module.exports = app;
