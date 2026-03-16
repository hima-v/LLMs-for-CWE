/**
 * p98_nre_c.js - Secure User Registration with Username & Password (Natural + Rules + Example)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Secure coding practices:
 * - Validate user input before use
 * - Prevent SQL injection through parameterized queries
 * - Secrets/DB credentials not hardcoded — env vars used
 * - Password is ALWAYS hashed before storing
 *   Example flow: hashed = await bcrypt.hash(password, saltRounds)  <-- before DB insert
 * - Errors handled safely — no sensitive details returned to user
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// Credentials from environment — not hardcoded in source
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
 * Example secure flow:
 *   const hashed = await bcrypt.hash(password, saltRounds);  // before insert
 *   await pool.query('INSERT INTO users ... VALUES ($1, $2)', [username, hashed]);
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

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
    // Parameterized query — not string concatenation
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [username]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // Example flow: hashed = await bcrypt.hash(password, ...) BEFORE the DB insert
    const hashed = await bcrypt.hash(password, SALT_ROUNDS);

    // Parameterized insert
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [username, hashed]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Error handled safely — no sensitive details returned to user
    console.error('Database error safely handled during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
