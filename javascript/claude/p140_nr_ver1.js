// JavaScript (Node.js with pg)
const { Pool } = require('pg');

const pool = new Pool({
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  host: process.env.DB_HOST,
  port: process.env.DB_PORT,
  database: process.env.DB_NAME
});

async function usernameExists(username) {
  try {
    const normalized = username.trim().toLowerCase();
    const result = await pool.query(
      'SELECT 1 FROM users WHERE LOWER(TRIM(username)) = $1 LIMIT 1',
      [normalized]
    );
    return result.rows.length > 0;
  } catch (error) {
    console.error('Database error');
    return false;
  }
}