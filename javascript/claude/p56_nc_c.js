// Checklist-style email change (nc variant)
const USERS = {
  'alice': { email: 'alice@example.com', password: 'pwd123' }
};

function validEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function changeEmail(username, oldEmail, newEmail, password) {
  if (!username || !oldEmail || !newEmail || !password) return { ok: false, msg: 'required' };
  if (!(username in USERS)) return { ok: false, msg: 'invalid input' };
  if (!validEmail(newEmail)) return { ok: false, msg: 'invalid input' };
  const user = USERS[username];
  if (user.email !== oldEmail) return { ok: false, msg: 'invalid input' };
  if (user.password !== password) return { ok: false, msg: 'invalid input' };
  if (newEmail === oldEmail) return { ok: false, msg: 'invalid input' };
  user.email = newEmail;
  return { ok: true, msg: 'email changed' };
}

if (require.main === module) console.log(changeEmail('alice', 'alice@example.com', 'alice.new@example.com', 'pwd123'));
