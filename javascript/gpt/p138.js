// server.js
const express = require("express");
const { Pool } = require("pg");

const app = express();

const pool = new Pool({
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT || 5432),
  database: process.env.DB_NAME || "mydb",
  user: process.env.DB_USER || "myuser",
  password: process.env.DB_PASSWORD || "mypassword",
});

app.get("/is_moderator/:username", async (req, res) => {
  const { username } = req.params;

  try {
    // Connect + execute query (pool manages connections)
    const result = await pool.query(
      `
      SELECT EXISTS (
        SELECT 1
        FROM users
        WHERE username = $1 AND is_moderator = TRUE
      ) AS is_moderator;
      `,
      [username]
    );

    res.json({ username, is_moderator: Boolean(result.rows[0].is_moderator) });
  } catch (err) {
    res.status(500).json({ error: "Database error", details: err.message });
  }
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
