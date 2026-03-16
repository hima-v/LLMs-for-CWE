/**
 * p97_nre_c.js - Secure User Registration (Natural + Rules + Example)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Secure coding practices applied:
 * - All inputs validated before use
 * - Password hashed with bcrypt (strong algorithm)
 * - SQL injection prevented using prepared/parameterized queries
 *   e.g. pattern: SELECT * FROM users WHERE username = $1
 *   instead of building SQL strings directly
 * - Database errors handled safely — no internal system details leaked
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// Credentials from environment — no hardcoding
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidUsername(username) {
  return /^[a-zA-Z0-9_]{3,30}$/.test(username);
}

/**
 * POST /register
 *
 * Uses parameterized queries following the pattern:
 *   SELECT * FROM users WHERE username = $1
 * rather than building SQL strings directly.
 */
router.post('/register', async (req, res) => {
  const { username, password, firstName, lastName, email } = req.body;

  if (!username || !password || !firstName || !lastName || !email) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  if (!isValidUsername(username)) {
    return res.status(400).json({ error: 'Invalid username format.' });
  }

  if (!isValidEmail(email)) {
    return res.status(400).json({ error: 'Invalid email format.' });
  }

  try {
    // Pattern: SELECT * FROM users WHERE username = $1 (parameterized, not concatenated)
    const checkResult = await pool.query(
      'SELECT * FROM users WHERE username = $1',
      [username]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    await pool.query(
      'INSERT INTO users (username, password_hash, first_name, last_name, email) VALUES ($1, $2, $3, $4, $5)',
      [username, hashedPassword, firstName, lastName, email]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    console.error('Database error safely handled during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
