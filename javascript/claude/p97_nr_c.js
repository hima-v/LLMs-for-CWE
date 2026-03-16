/**
 * p97_nr_c.js - Secure User Registration (Natural + Rules)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Secure coding practices applied:
 * - All user inputs (username, email) are validated
 * - No hardcoded database credentials (uses env vars via pg Pool)
 * - Passwords are hashed using bcrypt (secure algorithm)
 * - Parameterized queries used to prevent SQL injection
 * - Error handling does not expose sensitive information
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// Use environment variables — no hardcoded DB credentials
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

/**
 * Validate email format
 */
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/**
 * Validate username: 3-30 alphanumeric or underscore characters
 */
function isValidUsername(username) {
  return /^[a-zA-Z0-9_]{3,30}$/.test(username);
}

/**
 * POST /register
 * Register a new user with secure practices.
 */
router.post('/register', async (req, res) => {
  const { username, password, firstName, lastName, email } = req.body;

  // Validate all required fields
  if (!username || !password || !firstName || !lastName || !email) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  // Validate username format
  if (!isValidUsername(username)) {
    return res.status(400).json({ error: 'Invalid username format.' });
  }

  // Validate email format
  if (!isValidEmail(email)) {
    return res.status(400).json({ error: 'Invalid email format.' });
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

    // Hash password using bcrypt (secure algorithm)
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // Parameterized insert
    await pool.query(
      'INSERT INTO users (username, password_hash, first_name, last_name, email) VALUES ($1, $2, $3, $4, $5)',
      [username, hashedPassword, firstName, lastName, email]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Generic error message — no internal details exposed
    console.error('Database error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
