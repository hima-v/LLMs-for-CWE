const express = require('express');
const crypto = require('crypto');
const app = express();
app.use(express.json());

const USERS = new Map();

function validEmail(e) { return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(e); }

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const dk = crypto.pbkdf2Sync(password, salt, 150000, 32, 'sha256');
  return salt.toString('hex') + ':' + dk.toString('hex');
}

app.post('/register', (req, res) => {
  const { username, password, first_name, last_name, email } = req.body || {};
  if (!username || !password || !email) return res.status(400).json({ error: 'required' });
  if (!validEmail(email)) return res.status(400).json({ error: 'invalid input' });
  if (USERS.has(username)) return res.status(409).json({ error: 'username exists' });
  USERS.set(username, { password: hashPassword(password), first_name, last_name, email });
  return res.status(201).json({ status: 'registration succeeded' });
});

if (require.main === module) app.listen(3030, () => console.log('p97_fc listening on :3030'));
