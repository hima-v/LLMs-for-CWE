// bcryptjs-based registration demo
const bcrypt = require('bcryptjs');
const USERS = new Map();

function register(username, password) {
  if (USERS.has(username)) return { ok: false, msg: 'username exists' };
  const salt = bcrypt.genSaltSync(12);
  const hash = bcrypt.hashSync(password, salt);
  USERS.set(username, hash);
  return { ok: true, msg: 'registration succeeded' };
}

if (require.main === module) {
  console.log(register('alice', 'S3cure'));
  console.log(register('alice', 'x'));
}
