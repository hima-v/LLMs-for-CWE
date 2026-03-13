// Node.js + pg (PostgreSQL)
// npm i pg
// Set DB_URL env var.

const { Client } = require("pg");

function normalizeUsername(username, maxLen = 255) {
  const trimmed = (username ?? "").trim();
  const collapsed = trimmed.replace(/\s+/g, " ");
  // toLowerCase is locale-agnostic in JS; good enough for many cases.
  return collapsed.toLowerCase().slice(0, maxLen);
}

async function usernameExists(dbUrl, username) {
  const norm = normalizeUsername(username);
  if (!norm) return false;

  const client = new Client({ connectionString: dbUrl });
  try {
    await client.connect();

    // Parameterized query with $1
    const res = await client.query(
      "SELECT 1 FROM users WHERE username = $1 LIMIT 1",
      [norm]
    );

    return res.rowCount > 0;
  } catch (e) {
    // Do not leak sensitive info
    return false;
  } finally {
    try {
      await client.end();
    } catch (_) {
      // ignore
    }
  }
}

// Exit code: 0 => true, 2 => false, 1 => usage/config error
(async () => {
  const dbUrl = process.env.DB_URL || "";
  const username = process.argv[2];

  if (!dbUrl || !username) process.exit(1);

  const exists = await usernameExists(dbUrl, username);
  process.exit(exists ? 0 : 2);
})();