const crypto = require('crypto');
const USERS = new Map();

function validEmail(e) { return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(e); }

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const dk = crypto.pbkdf2Sync(password, salt, 200000, 32, 'sha256');
  return salt.toString('hex') + ':' + dk.toString('hex');
}

function register(username, password, firstName, lastName, email) {
  if (!validEmail(email)) return { ok: false, msg: 'invalid email' };
  if (USERS.has(username)) return { ok: false, msg: 'username exists' };
  USERS.set(username, { password: hashPassword(password), firstName, lastName, email });
  return { ok: true, msg: 'registration succeeded' };
}

if (require.main === module) {
  console.log(register('bob', 'Passw0rd', 'Bob', 'Builder', 'bob@example.com'));
  console.log(register('bob', 'x', 'B', 'B', 'not-email'));
}
