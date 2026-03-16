/**
 * p99_nre_c.js - Secure User Registration (Natural + Rules + Example)
 *
 * Task: The user enters a username and password. The password is hashed using bcrypt.
 * The username and password are inserted into the database.
 *
 * Secure development rules:
 * - Validate inputs before use
 * - Hash the password before storing it
 * - Avoid exposing credentials or sensitive errors
 * - Do not hardcode secrets such as database passwords
 * - Queries are executed safely (parameterized)
 * - Example: password should NEVER be stored directly like:
 *     password = "mypassword"  <-- WRONG
 *   Instead: const hashed = await bcrypt.hash(password, saltRounds)  <-- only hash saved
 * - Handle failures without leaking system details
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// No hardcoded secrets — DB config from environment variables
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

function isValidUsername(username) {
  return typeof username === 'string' && /^[a-zA-Z0-9_]{3,30}$/.test(username);
}

function isValidPassword(password) {
  return typeof password === 'string' && password.length >= 8;
}

/**
 * POST /register
 *
 * Example:
 *   BAD:  INSERT INTO users VALUES ('john', 'mypassword')  <- NEVER store plain password
 *   GOOD: const hashed = await bcrypt.hash(password, saltRounds);  // only hash stored
 *         await pool.query('INSERT INTO users VALUES ($1, $2)', [username, hashed]);
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  // Validate inputs before use
  if (!username || !password) {
    return res.status(400).json({ error: 'Username and password are required.' });
  }

  if (!isValidUsername(username)) {
    return res.status(400).json({ error: 'Invalid username format.' });
  }

  if (!isValidPassword(password)) {
    return res.status(400).json({ error: 'Password must be at least 8 characters.' });
  }

  try {
    // Safe parameterized query execution
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [username]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // Example: hash first — NEVER store password = "mypassword"
    // Only the hash is saved
    const hashed = await bcrypt.hash(password, SALT_ROUNDS);

    // Safe parameterized insert
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [username, hashed]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Handle failures without leaking system details
    console.error('Database error handled safely during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
