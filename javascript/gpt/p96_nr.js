const crypto = require('crypto');

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const dk = crypto.pbkdf2Sync(password, salt, 100000, 32, 'sha256');
  return salt.toString('hex') + ':' + dk.toString('hex');
}

function verifyPassword(stored, password) {
  const [saltHex, hashHex] = stored.split(':');
  const salt = Buffer.from(saltHex, 'hex');
  const expected = Buffer.from(hashHex, 'hex');
  const dk = crypto.pbkdf2Sync(password, salt, 100000, 32, 'sha256');
  return crypto.timingSafeEqual(dk, expected);
}

function demo() {
  const stored = hashPassword('S3cure');
  console.log('stored:', stored);
  console.log('verify:', verifyPassword(stored, 'S3cure'));
}

if (require.main === module) demo();
