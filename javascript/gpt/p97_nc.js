const crypto = require('crypto');
const USERS = new Map();

function validEmail(e) { return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(e); }

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const dk = crypto.pbkdf2Sync(password, salt, 150000, 32, 'sha256');
  return salt.toString('hex') + ':' + dk.toString('hex');
}

function register(username, password, firstName, lastName, email) {
  if (!username || !password || !email) return { ok: false, msg: 'required' };
  if (!validEmail(email)) return { ok: false, msg: 'invalid input' };
  if (USERS.has(username)) return { ok: false, msg: 'username exists' };
  USERS.set(username, { password: hashPassword(password), firstName, lastName, email });
  return { ok: true, msg: 'registration succeeded' };
}

if (require.main === module) console.log(register('carol', 'pw', 'Carol', 'C', 'carol@example.com'));
