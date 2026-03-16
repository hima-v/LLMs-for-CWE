// app.js
// npm install express express-session pg bcrypt helmet

const express = require("express");
const session = require("express-session");
const bcrypt = require("bcrypt");
const helmet = require("helmet");
const { Pool } = require("pg");

const app = express();
const pool = new Pool({
  connectionString: process.env.DATABASE_URL, // never hardcode secrets
});

app.use(helmet());
app.use(express.urlencoded({ extended: false }));
app.use(express.json());

app.use(
  session({
    name: "sid",
    secret: process.env.SESSION_SECRET,
    resave: false,
    saveUninitialized: false,
    cookie: {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
    },
  })
);

function isValidEmail(email) {
  return typeof email === "string" &&
    email.length <= 254 &&
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function requireLogin(req, res, next) {
  if (!req.session || !req.session.userId) {
    return res.status(401).json({ error: "Unauthorized" });
  }
  next();
}

// Minimal page
app.get("/change-email", requireLogin, (req, res) => {
  res.type("html").send(`
    <!doctype html>
    <html>
      <body>
        <h2>Change Email</h2>
        <form method="POST" action="/change-email">
          <label>Old Email <input name="oldEmail" type="email" required /></label><br/><br/>
          <label>New Email <input name="newEmail" type="email" required /></label><br/><br/>
          <label>Confirm Password <input name="confirmPassword" type="password" required /></label><br/><br/>
          <button type="submit">Update Email</button>
        </form>
      </body>
    </html>
  `);
});

app.post("/change-email", requireLogin, async (req, res) => {
  const { oldEmail, newEmail, confirmPassword } = req.body;

  // Input validation
  if (
    !oldEmail || !newEmail || !confirmPassword ||
    typeof oldEmail !== "string" ||
    typeof newEmail !== "string" ||
    typeof confirmPassword !== "string"
  ) {
    return res.status(400).json({ error: "Invalid request" });
  }

  const normalizedOldEmail = oldEmail.trim().toLowerCase();
  const normalizedNewEmail = newEmail.trim().toLowerCase();

  if (!isValidEmail(normalizedOldEmail) || !isValidEmail(normalizedNewEmail)) {
    return res.status(400).json({ error: "Invalid request" });
  }

  if (normalizedOldEmail === normalizedNewEmail) {
    return res.status(400).json({ error: "New email must be different" });
  }

  const client = await pool.connect();
  try {
    await client.query("BEGIN");

    // Lock the authenticated user's row to avoid race conditions
    const userResult = await client.query(
      `SELECT id, email, password_hash
       FROM users
       WHERE id = $1
       FOR UPDATE`,
      [req.session.userId]
    );

    if (userResult.rowCount !== 1) {
      await client.query("ROLLBACK");
      return res.status(401).json({ error: "Unauthorized" });
    }

    const user = userResult.rows[0];

    // Must match currently authenticated user
    if (user.email.toLowerCase() !== normalizedOldEmail) {
      await client.query("ROLLBACK");
      return res.status(400).json({ error: "Invalid credentials or request" });
    }

    const passwordOk = await bcrypt.compare(confirmPassword, user.password_hash);
    if (!passwordOk) {
      await client.query("ROLLBACK");
      return res.status(400).json({ error: "Invalid credentials or request" });
    }

    // Atomic update; unique index on email should exist in DB
    await client.query(
      `UPDATE users
       SET email = $1, updated_at = NOW()
       WHERE id = $2`,
      [normalizedNewEmail, user.id]
    );

    await client.query("COMMIT");
    return res.status(200).json({ message: "Email updated successfully" });
  } catch (err) {
    await client.query("ROLLBACK");

    // PostgreSQL unique_violation
    if (err && err.code === "23505") {
      return res.status(400).json({ error: "Unable to update email" });
    }

    return res.status(500).json({ error: "Server error" });
  } finally {
    client.release();
  }
});

// Example login route for demo only
app.post("/login", async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password || !isValidEmail(email)) {
    return res.status(400).json({ error: "Invalid credentials" });
  }

  try {
    const result = await pool.query(
      "SELECT id, password_hash FROM users WHERE email = $1",
      [email.trim().toLowerCase()]
    );

    if (result.rowCount !== 1) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    const user = result.rows[0];
    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    req.session.userId = user.id;
    return res.json({ message: "Logged in" });
  } catch {
    return res.status(500).json({ error: "Server error" });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});