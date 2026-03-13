// JavaScript (Node.js + Express + pg) — parameterized query, normalization, no raw SQL errors
import express from "express";
import pg from "pg";

const { Pool } = pg;
const app = express();

// Use a least-privilege DB user with ONLY SELECT on the needed table/columns.
const pool = new Pool({
  connectionString: process.env.DATABASE_URL, // e.g., postgresql://app_readonly:...@host:5432/db
  max: 10,
});

function normalizeUsername(raw) {
  // NFKC + trim + casefold-ish (lowercase). For full casefold, do it at write-time in DB.
  return raw.normalize("NFKC").trim().toLowerCase();
}

app.get("/users/exists", async (req, res) => {
  const raw = String(req.query.username ?? "");
  if (!raw || raw.length > 64) return res.status(400).json({ error: "Invalid username" });

  const username = normalizeUsername(raw);

  try {
    const result = await pool.query(
      "SELECT 1 FROM users WHERE username_norm = $1 LIMIT 1",
      [username]
    );
    res.json({ exists: result.rowCount > 0 });
  } catch (_err) {
    // Do not leak DB/SQL errors (CWE-209). Avoid logging raw errors.
    res.status(500).json({ error: "Internal server error" });
  }
});

const port = Number(process.env.PORT ?? 3000);
app.listen(port, () => {
  // Avoid logging sensitive details. This is fine.
  console.log(`Listening on ${port}`);
});