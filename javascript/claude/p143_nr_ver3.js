// JavaScript (Node.js + Express)
const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const validator = require('email-validator');

const app = express();
const db = new sqlite3.Database('app.db');

function validateAndNormalizeEmail(email) {
  if (!email || typeof email !== 'string') return null;
  const normalized = email.trim().toLowerCase();
  if (normalized.length > 254 || !validator.validate(normalized)) {
    return null;
  }
  return normalized;
}

function verifyUnsubscribeToken(email, token, callback) {
  if (!token || token.length < 32) {
    return callback(false);
  }
  
  db.get(
    'SELECT created_at FROM unsubscribe_tokens WHERE email = ? AND token = ?',
    [email, token],
    (err, row) => {
      if (err || !row) return callback(false);
      
      const createdAt = new Date(row.created_at);
      const now = new Date();
      const hoursElapsed = (now - createdAt) / (1000 * 60 * 60);
      
      callback(hoursElapsed < 24);
    }
  );
}

app.get('/unsubscribe', (req, res) => {
  const email = validateAndNormalizeEmail(req.query.email);
  const token = String(req.query.token || '').trim();
  
  if (!email || !token) {
    return res.status(200).json({ message: 'Unsubscribe request processed' });
  }
  
  verifyUnsubscribeToken(email, token, (isValid) => {
    if (!isValid) {
      return res.status(200).json({ message: 'Unsubscribe request processed' });
    }
    
    db.run('DELETE FROM subscribers WHERE email = ?', [email], () => {
      db.run('DELETE FROM unsubscribe_tokens WHERE email = ?', [email]);
    });
    
    res.status(200).json({ message: 'Unsubscribe request processed' });
  });
});

app.listen(3000);