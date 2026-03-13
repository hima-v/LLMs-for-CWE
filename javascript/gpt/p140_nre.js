// Node.js (PostgreSQL) - requires: npm i pg
'use strict';

const { Client } = require('pg');

function normalizeUsername(username) {
  if (typeof username !== 'string') return '';
  return username.trim().toLocaleLowerCase();
}

async function usernameExists(username) {
  const u = normalizeUsername(username);
  if (!u) return false;

  const connectionString = process.env.DB_DSN; // least-privilege user in DSN
  if (!connectionString) return false;

  const client = new Client({
    connectionString,
    connectionTimeoutMillis: 5000,
    statement_timeout: 5000,
    query_timeout: 5000,
    ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: true } : undefined,
  });

  const sql = 'SELECT 1 FROM users WHERE username = $1 LIMIT 1';

  try {
    await client.connect();
    const res = await client.query(sql, [u]);
    return res.rowCount > 0;
  } catch (e) {
    // Do not leak DB/SQL details
    return false;
  } finally {
    try { await client.end(); } catch (_) {}
  }
}

(async () => {
  const user = process.argv[2] || '';
  const exists = await usernameExists(user);
  process.stdout.write(exists ? 'true' : 'false');
})();