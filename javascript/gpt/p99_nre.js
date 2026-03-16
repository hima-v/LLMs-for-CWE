const bcrypt = require('bcryptjs');
const USERS = new Map();

function register(username, password) {
  if (USERS.has(username)) return { ok: false, msg: 'username exists' };
  const salt = bcrypt.genSaltSync(14);
  const hash = bcrypt.hashSync(password, salt);
  USERS.set(username, hash);
  return { ok: true, msg: 'registration succeeded' };
}

if (require.main === module) {
  console.log(register('bob', 'Str0ng'));
  console.log(register('bob', 'x'));
}
