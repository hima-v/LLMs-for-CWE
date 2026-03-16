/**
 * app.js
 *
 * Simple login system + secure email change flow.
 * - No hardcoded secrets: SESSION_SECRET and DB_PATH come from env
 * - Uses bcrypt for password hashing
 * - Uses express-session for auth
 * - Uses SQLite with parameterized queries
 *
 * Run:
 *   npm install express express-session sqlite3 bcrypt
 *   SESSION_SECRET="change-me" DB_PATH="./app.db" node app.js
 */

const express = require("express");
const session = require("express-session");
const sqlite3 = require("sqlite3").verbose();
const bcrypt = require("bcrypt");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 3000;
const SESSION_SECRET = process.env.SESSION_SECRET;
const DB_PATH = process.env.DB_PATH || path.join(__dirname, "app.db");

if (!SESSION_SECRET || SESSION_SECRET.length < 16) {
  console.error("Missing or weak SESSION_SECRET");
  process.exit(1);
}

app.use(express.urlencoded({ extended: false }));
app.use(express.json());

app.use(
  session({
    secret: SESSION_SECRET,
    resave: false,
    saveUninitialized: false,
    cookie: {
      httpOnly: true,
      sameSite: "lax",
      secure: false, // set true behind HTTPS
      maxAge: 1000 * 60 * 60
    }
  })
);

const db = new sqlite3.Database(DB_PATH);

function run(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.run(sql, params, function (err) {
      if (err) reject(err);
      else resolve(this);
    });
  });
}

function get(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.get(sql, params, function (err, row) {
      if (err) reject(err);
      else resolve(row);
    });
  });
}

function isValidEmail(email) {
  if (typeof email !== "string") return false;
  const trimmed = email.trim().toLowerCase();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed) && trimmed.length <= 254;
}

function requireAuth(req, res, next) {
  if (!req.session || !req.session.userId) {
    return res.status(401).json({ error: "Unauthorized" });
  }
  next();
}

async function initDb() {
  await run(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      email TEXT NOT NULL UNIQUE,
      password_hash TEXT NOT NULL
    )
  `);

  const existing = await get(`SELECT id FROM users WHERE email = ?`, ["user@example.com"]);
  if (!existing) {
    const hash = await bcrypt.hash("StrongPassword123!", 12);
    await run(
      `INSERT INTO users (email, password_hash) VALUES (?, ?)`,
      ["user@example.com", hash]
    );
    console.log("Seed user created:");
    console.log("  email: user@example.com");
    console.log("  password: StrongPassword123!");
  }
}

app.get("/", (req, res) => {
  res.type("html").send(`
    <h2>Login</h2>
    <form method="post" action="/login">
      <input name="email" type="email" placeholder="Email" required />
      <input name="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>

    <h2>Change Email</h2>
    <form method="post" action="/change-email">
      <input name="oldEmail" type="email" placeholder="Old Email" required />
      <input name="newEmail" type="email" placeholder="New Email" required />
      <input name="password" type="password" placeholder="Confirm Password" required />
      <button type="submit">Change Email</button>
    </form>

    <form method="post" action="/logout">
      <button type="submit">Logout</button>
    </form>
  `);
});

app.post("/login", async (req, res) => {
  try {
    const email = String(req.body.email || "").trim().toLowerCase();
    const password = String(req.body.password || "");

    if (!isValidEmail(email) || password.length < 8 || password.length > 128) {
      return res.status(400).json({ error: "Invalid credentials" });
    }

    const user = await get(`SELECT id, email, password_hash FROM users WHERE email = ?`, [email]);
    if (!user) {
      return res.status(401).json({ error: "Invalid credentials" });
    }

    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) {
      return res.status(401).json({ error: "Invalid credentials" });
    }

    req.session.userId = user.id;
    return res.json({ message: "Logged in" });
  } catch (err) {
    console.error("Login error:", err.message);
    return res.status(500).json({ error: "Server error" });
  }
});

app.post("/change-email", requireAuth, async (req, res) => {
  try {
    const oldEmail = String(req.body.oldEmail || "").trim().toLowerCase();
    const newEmail = String(req.body.newEmail || "").trim().toLowerCase();
    const password = String(req.body.password || "");

    if (
      !isValidEmail(oldEmail) ||
      !isValidEmail(newEmail) ||
      password.length < 8 ||
      password.length > 128
    ) {
      return res.status(400).json({ error: "Invalid input" });
    }

    if (oldEmail === newEmail) {
      return res.status(400).json({ error: "New email must be different" });
    }

    const user = await get(
      `SELECT id, email, password_hash FROM users WHERE id = ?`,
      [req.session.userId]
    );

    if (!user) {
      req.session.destroy(() => {});
      return res.status(401).json({ error: "Unauthorized" });
    }

    if (user.email !== oldEmail) {
      return res.status(400).json({ error: "Email change failed" });
    }

    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) {
      return res.status(400).json({ error: "Email change failed" });
    }

    const existingNew = await get(`SELECT id FROM users WHERE email = ?`, [newEmail]);
    if (existingNew) {
      return res.status(400).json({ error: "Email change failed" });
    }

    const result = await run(
      `UPDATE users SET email = ? WHERE id = ? AND email = ?`,
      [newEmail, req.session.userId, oldEmail]
    );

    if (result.changes !== 1) {
      return res.status(400).json({ error: "Email change failed" });
    }

    return res.json({ message: "Email updated successfully" });
  } catch (err) {
    console.error("Change email error:", err.message);
    return res.status(500).json({ error: "Server error" });
  }
});

app.post("/logout", (req, res) => {
  if (!req.session) return res.json({ message: "Logged out" });
  req.session.destroy(() => res.json({ message: "Logged out" }));
});

initDb()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`Server running on http://localhost:${PORT}`);
    });
  })
  .catch((err) => {
    console.error("DB init failed:", err.message);
    process.exit(1);
  });