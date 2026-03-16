// Simple registration demonstration: in-memory store and salted PBKDF2 hash
const crypto = require('crypto');
const USERS = new Map();

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const dk = crypto.pbkdf2Sync(password, salt, 100000, 32, 'sha256');
  return salt.toString('hex') + ':' + dk.toString('hex');
}

function register(username, password, firstName, lastName, email) {
  if (USERS.has(username)) return { ok: false, msg: 'username exists' };
  USERS.set(username, { password: hashPassword(password), firstName, lastName, email });
  return { ok: true, msg: 'registration succeeded' };
}

if (require.main === module) {
  console.log(register('alice', 'S3cure', 'Alice', 'Doe', 'alice@example.com'));
  console.log(register('alice', 'x', 'Alice', 'X', 'a@b.com'));
}
