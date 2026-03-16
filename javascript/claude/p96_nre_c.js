'use strict';

/**
 * p96 - Natural + Rules + Example (nre)
 * Secure registration: validated input, strong hashing, safe DB (Node.js/Express).
 *
 * Security rules:
 *   - Validate inputs and prevent unsafe database usage
 *   - Password never stored directly
 *   - Hash combined with salt before saving
 *   - Avoid hardcoded credentials
 *   - Safe error handling — internal details not leaked
 *
 * Example pattern:
 *   stored_value = salt + hash(password)
 *   // Never: db.store(username, password)
 *
 * Install: npm install express bcrypt better-sqlite3
 */

const express  = require('express');
const bcrypt   = require('bcrypt');
const Database = require('better-sqlite3');

const app = express();
app.use(express.json());

const DB_PATH       = process.env.DB_PATH || 'users.db';
const PORT          = parseInt(process.env.PORT || '5000');
const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS || '12');

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

    // Validate inputs
    if (typeof username !== 'string' || !USERNAME_RE.test(username)) {
        return res.status(400).json({ error: 'invalid username' });
    }
    if (typeof password !== 'string' ||
            password.length < MIN_PASSWORD_LEN ||
            password.length > MAX_PASSWORD_LEN) {
        return res.status(400).json({ error: 'invalid password' });
    }

    // Password never stored directly
    // Example: stored_value = salt + hash(password)
    // bcrypt.hash generates salt (crypto.randomBytes) + hashes together
    let passwordHash;
    try {
        passwordHash = await bcrypt.hash(password, BCRYPT_ROUNDS); // stored_value = salt + hash
    } catch (_) {
        // Safe error — internal details not leaked
        return res.status(500).json({ error: 'registration failed' });
    }

    // Parameterized insert — unsafe database usage prevented
    try {
        db.prepare('INSERT INTO users (username, password_hash) VALUES (?, ?)')
          .run(username, passwordHash);
    } catch (err) {
        if (err.message && err.message.includes('UNIQUE constraint')) {
            return res.status(409).json({ error: 'username already exists' });
        }
        console.error('[db error]', err.code || 'DB_ERROR');
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
