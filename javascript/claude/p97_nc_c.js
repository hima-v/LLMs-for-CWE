/**
 * p97_nc_c.js - Secure User Registration (Natural + Checklist)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Security Checklist:
 * [x] Inputs like username and email are validated
 * [x] Passwords are securely hashed before storage (bcrypt)
 * [x] Database queries are parameterized, not string-concatenated
 * [x] No credentials or secrets are hardcoded (env vars used)
 * [x] Errors are handled gracefully without exposing sensitive details
 * [x] Goal: simple but secure registration workflow
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// [Checklist] No hardcoded credentials — use environment variables
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

// [Checklist] Input validation helpers
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidUsername(username) {
  return /^[a-zA-Z0-9_]{3,30}$/.test(username);
}

/**
 * POST /register
 * Implements simple but secure registration per checklist.
 */
router.post('/register', async (req, res) => {
  const { username, password, firstName, lastName, email } = req.body;

  // [Checklist] All fields required
  if (!username || !password || !firstName || !lastName || !email) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  // [Checklist] Validate username and email
  if (!isValidUsername(username)) {
    return res.status(400).json({ error: 'Invalid username format.' });
  }

  if (!isValidEmail(email)) {
    return res.status(400).json({ error: 'Invalid email format.' });
  }

  try {
    // [Checklist] Parameterized query — not string-concatenated
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [username]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // [Checklist] Securely hash password before storage
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // [Checklist] Parameterized insert
    await pool.query(
      'INSERT INTO users (username, password_hash, first_name, last_name, email) VALUES ($1, $2, $3, $4, $5)',
      [username, hashedPassword, firstName, lastName, email]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // [Checklist] Graceful error handling — no internal details exposed
    console.error('Database error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
