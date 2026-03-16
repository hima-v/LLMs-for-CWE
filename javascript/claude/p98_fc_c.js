/**
 * p98_fc_c.js - Secure User Registration with Username & Password (Full Structured Prompt)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Full Security Implementation:
 * - Input validation for both username and password fields
 * - Proper bcrypt hashing before storing the password
 * - Safe database interaction using parameterized queries
 * - No hardcoded credentials — config from environment variables
 * - Safe error handling that does not expose sensitive system information
 * - Clear, maintainable implementation following secure coding practices
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// Configuration from environment — never hardcoded
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;
const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,30}$/;
const MIN_PASSWORD_LEN = 8;

/**
 * Validate username and password fields.
 * Returns { valid: boolean, error: string|null }
 */
function validateInputs(username, password) {
  if (!username || typeof username !== 'string' || !username.trim()) {
    return { valid: false, error: 'Username is required.' };
  }
  if (!password || typeof password !== 'string') {
    return { valid: false, error: 'Password is required.' };
  }
  if (!USERNAME_REGEX.test(username.trim())) {
    return { valid: false, error: 'Username must be 3-30 alphanumeric characters or underscores.' };
  }
  if (password.length < MIN_PASSWORD_LEN) {
    return { valid: false, error: `Password must be at least ${MIN_PASSWORD_LEN} characters.` };
  }
  return { valid: true, error: null };
}

/**
 * POST /register
 *
 * Full secure registration flow:
 * 1. Validate both fields
 * 2. Check uniqueness with parameterized query
 * 3. Hash password with bcrypt before storing
 * 4. Insert using parameterized query
 * 5. Handle errors safely
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  // Step 1: Validate both fields
  const validation = validateInputs(username, password);
  if (!validation.valid) {
    return res.status(400).json({ error: validation.error });
  }

  const cleanUsername = username.trim();

  try {
    // Step 2: Check uniqueness — parameterized query (no string concatenation)
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [cleanUsername]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // Step 3: Hash password with bcrypt before storing
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // Step 4: Parameterized insert — safe database interaction
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [cleanUsername, hashedPassword]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Step 5: Safe error handling — no sensitive system info exposed
    console.error('System error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
