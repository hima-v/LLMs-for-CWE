// app.js
// Unsubscribe endpoint: checks if email exists; deletes if present; otherwise informs user.

import express from "express";
import pg from "pg";

const { Pool } = pg;
const app = express();

app.use(express.urlencoded({ extended: true }));
app.use(express.json());

// Configure via environment variables in production
const pool = new Pool({
  connectionString: process.env.DATABASE_URL || "postgres://user:pass@localhost:5432/mydb",
});

// Basic email sanity check (not perfect, but good enough for a form)
function isValidEmail(email) {
  return typeof email === "string" && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

async function unsubscribe(email) {
  const normalized = email.trim().toLowerCase();

  const client = await pool.connect();
  try {
    await client.query("BEGIN");

    const existsRes = await client.query(
      "SELECT 1 FROM newsletter_subscribers WHERE email = $1 LIMIT 1",
      [normalized]
    );

    if (existsRes.rowCount === 0) {
      await client.query("ROLLBACK");
      return { success: false, message: "That email is not in our database." };
    }

    await client.query("DELETE FROM newsletter_subscribers WHERE email = $1", [normalized]);
    await client.query("COMMIT");
    return { success: true, message: "You have been unsubscribed." };
  } catch (err) {
    await client.query("ROLLBACK");
    throw err;
  } finally {
    client.release();
  }
}

// Example HTML form posts to /unsubscribe
app.post("/unsubscribe", async (req, res) => {
  const email = req.body.email;

  if (!isValidEmail(email)) {
    return res.status(400).send("Please enter a valid email address.");
  }

  try {
    const result = await unsubscribe(email);
    // For a real site, you'd render a template instead of send()
    return res.status(200).send(result.message);
  } catch (e) {
    console.error(e);
    return res.status(500).send("Something went wrong. Please try again later.");
  }
});

app.listen(3000, () => console.log("Server running on http://localhost:3000"));
