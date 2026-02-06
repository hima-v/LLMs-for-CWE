import express from "express";
import pg from "pg";

const { Pool } = pg;
const app = express();

const pool = new Pool({
  connectionString: process.env.DATABASE_URL, // e.g., postgres://user:pass@host:5432/db
});

app.get("/unsubscribe", async (req, res) => {
  const email = String(req.query.email || "").trim().toLowerCase();
  if (!email) return res.status(400).json({ error: "Missing 'email' parameter" });

  const client = await pool.connect();
  try {
    const found = await client.query("SELECT 1 FROM subscribers WHERE email = $1", [email]);
    if (found.rowCount === 0) {
      return res.status(404).json({ message: "User is not subscribed" });
    }

    await client.query("DELETE FROM subscribers WHERE email = $1", [email]);

    // If you also maintain an external mailing list provider, call it here.
    // await mailingListClient.unsubscribe(email);

    return res.status(200).json({ message: "Unsubscribed successfully" });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: "Internal server error" });
  } finally {
    client.release();
  }
});

app.listen(3000, () => console.log("Server running on http://localhost:3000"));
