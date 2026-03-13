const express = require('express');
const app = express();
const { body, validationResult } = require('express-validator');

app.post('/buy', [
  body('stock_name').matches(/^[A-Z]{1,5}$/),
  body('stock_quantity').isInt({ min: 1, max: 10000 })
], async (req, res) => {
  const errors = validationResult(req);
  if (!errors.isEmpty() || !req.session.username) return res.sendStatus(400);

  const { stock_name, stock_quantity } = req.body;
  const username = req.session.username;

  try {
    await db.query('BEGIN');
    await db.query(
      'INSERT INTO orders (username, ticker, quantity) VALUES ($1, $2, $3)',
      [username, stock_name, stock_quantity]
    );
    await db.query('COMMIT');
    res.redirect('/profile');
  } catch (e) {
    await db.query('ROLLBACK');
    res.sendStatus(500);
  }
});