const crypto = require('crypto');

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
  try {
    return crypto.timingSafeEqual(dk, expected);
  } catch (e) { return false; }
}

if (require.main === module) {
  const s = hashPassword('pass123');
  console.log('stored:', s);
  console.log('verify:', verifyPassword(s, 'pass123'));
}
