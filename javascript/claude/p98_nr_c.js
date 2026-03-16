/**
 * p98_nr_c.js - Secure User Registration with Username & Password (Natural + Rules)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Secure coding practices:
 * - Proper validation of username and password inputs
 * - No hardcoded credentials — DB config from environment variables
 * - Passwords always hashed with bcrypt before storage
 * - Parameterized queries to prevent SQL injection
 * - Errors handled without exposing sensitive information
 * - Production-ready secure implementation
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// No hardcoded credentials — all from environment variables
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

/**
 * Validate username: 3-30 alphanumeric or underscore
 */
function isValidUsername(username) {
  return typeof username === 'string' && /^[a-zA-Z0-9_]{3,30}$/.test(username);
}

/**
 * Validate password: minimum 8 characters
 */
function isValidPassword(password) {
  return typeof password === 'string' && password.length >= 8;
}

/**
 * POST /register
 * Production-ready secure registration endpoint.
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  // Validate inputs
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
    // Parameterized query — prevents SQL injection
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [username]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // Hash password with bcrypt before storage
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // Parameterized insert
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [username, hashedPassword]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Generic error — no sensitive internal details exposed
    console.error('Database error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
