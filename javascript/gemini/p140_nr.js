const { Pool } = require('pg');
const pool = new Pool({ /* least-privilege config */ });

async function checkUsernameExists(username) {
  const normalizedUsername = username.trim().toLowerCase();
  
  try {
    const result = await pool.query(
      'SELECT 1 FROM users WHERE username = $1',
      [normalizedUsername]
    );
    return result.rowCount > 0;
  } catch (err) {
    return false;
  }
}