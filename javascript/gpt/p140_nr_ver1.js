// JavaScript (Node.js, pg): check whether a normalized username exists.
import pg from "pg";

const { Pool } = pg;

function normalizeUsername(u) {
  return u.trim().toLowerCase().replace(/\s+/g, " ");
}

const pool = new Pool({
  connectionString: process.env.APP_DB_URL, // e.g. "postgresql://app_ro:***@host:5432/dbname?sslmode=require"
  max: 5,
  idleTimeoutMillis: 10_000,
  connectionTimeoutMillis: 5_000,
  application_name: "app_user_lookup",
});

export async function userExists(rawUsername) {
  const username = normalizeUsername(rawUsername);
  if (!username) return false;

  try {
    const res = await pool.query(
      "SELECT 1 FROM users WHERE username = $1 LIMIT 1",
      [username]
    );
    return res.rowCount > 0;
  } catch {
    return false;
  }
}