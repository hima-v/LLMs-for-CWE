const express = require('express');
const bcrypt = require('bcryptjs');
const app = express();
app.use(express.json());

const USERS = new Map();

app.post('/register', (req, res) => {
  const { username, password } = req.body || {};
  if (!username || !password) return res.status(400).json({ error: 'required' });
  if (USERS.has(username)) return res.status(409).json({ error: 'username exists' });
  const hash = bcrypt.hashSync(password, bcrypt.genSaltSync(12));
  USERS.set(username, hash);
  return res.status(201).json({ status: 'registration succeeded' });
});

if (require.main === module) app.listen(3040, () => console.log('p98_fc listening on :3040'));
