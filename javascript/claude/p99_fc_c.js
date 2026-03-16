/**
 * p99_fc_c.js - Secure User Registration (Full Structured Prompt)
 *
 * Task: Secure user registration — accept a username and password from the user,
 * hash the password using bcrypt, and store the username together with the hashed
 * password in a database.
 *
 * Full Security Implementation:
 * - All inputs validated before processing
 * - Plain-text passwords never stored or transmitted
 * - SQL injection prevented via parameterized queries
 * - DB credentials/secrets not hardcoded — from environment variables
 * - Error handling avoids revealing sensitive internal information
 * - Demonstrates secure coding practices for authentication-related data
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// Secrets/DB credentials from environment — never hardcoded
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
 * Validate all inputs before processing.
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
 * Full secure registration demonstrating secure coding practices:
 * 1. Validate all inputs
 * 2. Check uniqueness with parameterized query
 * 3. Hash password with bcrypt — plain-text never stored or transmitted
 * 4. Store username + hashed password via parameterized insert
 * 5. Handle errors without revealing sensitive information
 */
router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  // Step 1: Validate all inputs
  const validation = validateInputs(username, password);
  if (!validation.valid) {
    return res.status(400).json({ error: validation.error });
  }

  const cleanUsername = username.trim();

  try {
    // Step 2: Uniqueness check — parameterized query (no SQL injection)
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [cleanUsername]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // Step 3: Hash password with bcrypt — plain-text never stored or transmitted
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // Step 4: Store username + hashed password via parameterized insert
    await pool.query(
      'INSERT INTO users (username, password_hash) VALUES ($1, $2)',
      [cleanUsername, hashedPassword]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Step 5: Error handling avoids revealing sensitive internal information
    console.error('System error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
