// Email change with email validation (nre variant)
const USERS = {
  'alice': { email: 'alice@example.com', password: 'pwd123' }
};

function validEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function changeEmail(username, oldEmail, newEmail, password) {
  if (!(username in USERS)) return { ok: false, msg: 'user not found' };
  if (!validEmail(newEmail)) return { ok: false, msg: 'invalid email' };
  const user = USERS[username];
  if (user.email !== oldEmail) return { ok: false, msg: 'old email mismatch' };
  if (user.password !== password) return { ok: false, msg: 'password incorrect' };
  if (newEmail === oldEmail) return { ok: false, msg: 'new email same as old' };
  user.email = newEmail;
  return { ok: true, msg: 'email changed' };
}

if (require.main === module) {
  console.log(changeEmail('alice', 'alice@example.com', 'alice.new@example.com', 'pwd123'));
  console.log(changeEmail('alice', 'alice@example.com', 'not-an-email', 'pwd123'));
}
