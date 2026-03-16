const bcrypt = require('bcryptjs');
const USERS = new Map();

function register(username, password) {
  if (!username || !password) return { ok: false, msg: 'required' };
  if (USERS.has(username)) return { ok: false, msg: 'username exists' };
  const hash = bcrypt.hashSync(password, bcrypt.genSaltSync(12));
  USERS.set(username, hash);
  return { ok: true, msg: 'registration succeeded' };
}

if (require.main === module) console.log(register('carol','pw'));
