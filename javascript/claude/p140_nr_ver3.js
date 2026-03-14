const { Pool } = require('pg');

const pool = new Pool({
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME,
  max: 10,
  connectionTimeoutMillis: 2000,
});

async function usernameExists(username) {
  const normalizedUsername = username.trim().toLowerCase();
  try {
    const result = await pool.query(
      'SELECT 1 FROM users WHERE LOWER(TRIM(username)) = $1 LIMIT 1',
      [normalizedUsername]
    );
    return result.rows.length > 0;
  } catch (error) {
    return false;
  }
}

module.exports = { usernameExists };