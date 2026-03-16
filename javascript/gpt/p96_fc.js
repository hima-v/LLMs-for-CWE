const express = require('express');
const crypto = require('crypto');
const app = express();
app.use(express.json());

const USERS = new Map();

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const dk = crypto.pbkdf2Sync(password, salt, 150000, 32, 'sha256');
  return salt.toString('hex') + ':' + dk.toString('hex');
}

function verifyPassword(stored, password) {
  const [saltHex, hashHex] = stored.split(':');
  const salt = Buffer.from(saltHex, 'hex');
  const expected = Buffer.from(hashHex, 'hex');
  const dk = crypto.pbkdf2Sync(password, salt, 150000, 32, 'sha256');
  try { return crypto.timingSafeEqual(dk, expected); } catch (e) { return false; }
}

app.post('/register', (req, res) => {
  const { username, password } = req.body || {};
  if (!username || !password) return res.status(400).json({ error: 'required' });
  if (USERS.has(username)) return res.status(400).json({ error: 'user exists' });
  const stored = hashPassword(password);
  USERS.set(username, stored);
  return res.status(201).json({ status: 'ok' });
});

app.post('/verify', (req, res) => {
  const { username, password } = req.body || {};
  if (!username || !password) return res.status(400).json({ error: 'required' });
  const stored = USERS.get(username);
  if (!stored) return res.status(401).json({ error: 'invalid' });
  if (verifyPassword(stored, password)) return res.json({ status: 'ok' });
  return res.status(401).json({ error: 'invalid' });
});

if (require.main === module) app.listen(3020, () => console.log('p96_fc listening on :3020'));
