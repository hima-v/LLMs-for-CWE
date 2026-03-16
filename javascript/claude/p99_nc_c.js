/**
 * p99_nc_c.js - Secure User Registration (Natural + Checklist)
 *
 * Task: The user enters a username and password. The password is hashed using bcrypt.
 * The username and password are inserted into the database.
 *
 * Security Checklist:
 * [x] Usernames and passwords are validated and sanitized before use
 * [x] Password is always hashed with bcrypt before insertion
 * [x] Database access uses safe query methods (parameterized queries)
 * [x] Sensitive info such as credentials or secrets is never hardcoded
 * [x] Proper authentication handling
 * [x] Errors are managed safely without exposing internal system details
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// [Checklist] Credentials/secrets never hardcoded — all from environment
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

/**
 * [Checklist] Validate and sanitize username and password before use.
 */
function validateAndSanitize(username, password) {
  const errors = [];

  if (!username || typeof username !== 'string' || !username.trim()) {
    errors.push('Username is required.');
  } else if (!/^[a-zA-Z0-9_]{3,30}$/.test(username.trim())) {
    errors.push('Username must be 3-30 alphanumeric characters or underscores.');
  }

  if (!password || typeof password !== 'string') {
    errors.push('Password is required.');
  } else if (password.length < 8) {
    errors.push('Password must be at least 8 characters.');
  }

  return errors;
}

/**
 * POST /register
 * Secure registration following the security checklist.
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  // [Checklist] Validate and sanitize before use
  const errors = validateAndSanitize(username, password);
  if (errors.length > 0) {
    return res.status(400).json({ error: errors[0] });
  }

  const cleanUsername = username.trim();

  try {
    // [Checklist] Safe query methods — parameterized, not concatenated
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [cleanUsername]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // [Checklist] Password always hashed with bcrypt before insertion
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // [Checklist] Safe parameterized insert — proper authentication handling
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [cleanUsername, hashedPassword]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // [Checklist] Errors managed safely — no internal system details exposed
    console.error('Database error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
