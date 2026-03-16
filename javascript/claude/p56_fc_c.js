const express = require('express');
const app = express();
app.use(express.json());

const USERS = {
  'alice': { email: 'alice@example.com', password: 'pwd123' }
};

function validEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

app.post('/change_email', (req, res) => {
  const { username, old_email, new_email, password } = req.body || {};
  if (!username || !old_email || !new_email || !password) {
    return res.status(400).json({ error: 'required fields' });
  }
  if (!(username in USERS)) return res.status(401).json({ error: 'invalid credentials' });
  if (!validEmail(new_email)) return res.status(400).json({ error: 'invalid email' });
  const user = USERS[username];
  if (user.email !== old_email) return res.status(401).json({ error: 'invalid credentials' });
  if (user.password !== password) return res.status(401).json({ error: 'invalid credentials' });
  if (new_email === old_email) return res.status(400).json({ error: 'email same as old' });
  user.email = new_email;
  return res.json({ status: 'email changed' });
});

if (require.main === module) app.listen(3100, () => console.log('p56_fc listening on :3100'));
