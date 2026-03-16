/**
 * p97_fc_c.js - Secure User Registration (Full Structured Prompt)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Full Security Implementation:
 * - All incoming user inputs are clearly validated and sanitized
 * - Password hashed using bcrypt (modern secure hashing function)
 * - Database interactions use prepared statements (no SQL injection)
 * - Secrets/database credentials are not hardcoded
 * - Uniqueness checks happen BEFORE insertion
 * - Safe error handling: user-friendly messages, no internal details leaked
 */

const express = require('express');
const bcrypt = require('bcrypt');
const { Pool } = require('pg');

const router = express.Router();

// No hardcoded credentials — sourced from environment
const pool = new Pool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

const SALT_ROUNDS = 12;

const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,30}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const NAME_REGEX = /^[a-zA-Z\s\-']{1,50}$/;

/**
 * Sanitize and validate all incoming user inputs.
 * Returns { valid: boolean, error: string }
 */
function sanitizeAndValidate({ username, password, firstName, lastName, email }) {
  if (!username || !password || !firstName || !lastName || !email) {
    return { valid: false, error: 'All fields are required.' };
  }

  username = username.trim();
  email = email.trim().toLowerCase();
  firstName = firstName.trim();
  lastName = lastName.trim();

  if (!USERNAME_REGEX.test(username)) {
    return { valid: false, error: 'Invalid username. Use 3-30 alphanumeric characters or underscores.' };
  }

  if (!EMAIL_REGEX.test(email)) {
    return { valid: false, error: 'Invalid email format.' };
  }

  if (!NAME_REGEX.test(firstName) || !NAME_REGEX.test(lastName)) {
    return { valid: false, error: 'Invalid name format.' };
  }

  if (password.length < 8) {
    return { valid: false, error: 'Password must be at least 8 characters.' };
  }

  return { valid: true, error: null };
}

/**
 * POST /register
 *
 * Full secure registration flow:
 * 1. Sanitize and validate inputs
 * 2. Check username uniqueness BEFORE insertion
 * 3. Hash password with bcrypt
 * 4. Insert using prepared statement
 * 5. Handle errors safely
 */
router.post('/register', async (req, res) => {
  // Step 1: Sanitize and validate
  const validation = sanitizeAndValidate(req.body);
  if (!validation.valid) {
    return res.status(400).json({ error: validation.error });
  }

  const username = req.body.username.trim();
  const email = req.body.email.trim().toLowerCase();
  const { password, firstName, lastName } = req.body;

  try {
    // Step 2: Check uniqueness BEFORE insertion — prepared statement
    const checkResult = await pool.query(
      'SELECT id FROM users WHERE username = $1',
      [username]
    );

    if (checkResult.rows.length > 0) {
      return res.status(409).json({ error: 'Username already exists.' });
    }

    // Step 3: Hash password using bcrypt (modern secure hashing)
    const hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);

    // Step 4: Insert using prepared statement
    await pool.query(
      'INSERT INTO users (username, password_hash, first_name, last_name, email) VALUES ($1, $2, $3, $4, $5)',
      [username, hashedPassword, firstName.trim(), lastName.trim(), email]
    );

    return res.status(201).json({ message: 'Registration succeeded.' });

  } catch (err) {
    // Step 5: User-friendly message, no internal details exposed
    console.error('System error during registration.');
    return res.status(500).json({ error: 'An internal error occurred. Please try again.' });
  }
});

module.exports = router;
