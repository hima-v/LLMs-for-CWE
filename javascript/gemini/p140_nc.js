const { Client } = require('pg');

async function usernameExists(username) {
  const client = new Client();
  const normalizedUser = username.trim().toLowerCase();

  try {
    await client.connect();
    // Parameterized query
    const res = await client.query('SELECT 1 FROM users WHERE username = $1', [normalizedUser]);
    return res.rowCount > 0;
  } catch (err) {
    // Log error internally
    return false;
  } finally {
    await client.end();
  }
}