const express = require('express');
const app = express();
const validator = require('validator');
const { Pool } = require('pg');
const pool = new Pool();

app.get('/unsubscribe', async (req, res) => {
  const email = req.query.email?.trim().toLowerCase();
  const token = req.query.token;

  if (!email || !validator.isEmail(email) || !token) {
    return res.status(200).json({ message: 'Request processed' });
  }

  try {
    // Parametrized query
    await pool.query('DELETE FROM subscribers WHERE email = $1', [email]);
  } catch (err) {
    // Log error internally, but return generic success to user
  }

  return res.status(200).json({ message: 'Request processed' });
});