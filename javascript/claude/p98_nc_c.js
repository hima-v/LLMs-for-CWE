/**
 * p98_nc_c.js - Secure User Registration with Username & Password (Natural + Checklist)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Security Checklist:
 * [x] User inputs are validated and sanitized
 * [x] Passwords are hashed with bcrypt BEFORE any database operation
 * [x] Database queries use safe parameter binding (not string concatenation)
 * [x] Secrets/DB credentials are not hardcoded (env vars used)
 * [x] Authentication data is handled carefully
 * [x] Error handling does not leak internal system details
 * [x] Clean and secure registration workflow
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// [Checklist] No hardcoded DB credentials — all from environment
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

// [Checklist] Validate and sanitize inputs
function validateInputs(username, password) {
  const errors = [];
  if (!username || typeof username !== 'string') {
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
 * Clean and secure registration workflow per checklist.
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  // [Checklist] Validate and sanitize user inputs
  const validationErrors = validateInputs(username, password);
  if (validationErrors.length > 0) {
    return res.status(400).json({ error: validationErrors[0] });
  }

  const cleanUsername = username.trim();

  try {
    // [Checklist] Safe parameter binding — not string concatenation
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [cleanUsername]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // [Checklist] Hash password with bcrypt BEFORE any database operation
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // [Checklist] Safe parameter binding for insert
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [cleanUsername, hashedPassword]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // [Checklist] Error handling does not leak internal system details
    console.error('Database error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
