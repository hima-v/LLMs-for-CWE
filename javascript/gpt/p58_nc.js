// app.js
// Run:
//   npm i express express-session bcrypt better-sqlite3
//   SESSION_SECRET="replace_me_with_a_long_random_secret" node app.js
//
// Demo login account must already exist in DB.
// This file creates the table if needed but does NOT hardcode demo passwords.

const express = require("express");
const session = require("express-session");
const bcrypt = require("bcrypt");
const Database = require("better-sqlite3");

const app = express();
const db = new Database("app.db");

const PORT = process.env.PORT || 3000;
const SESSION_SECRET = process.env.SESSION_SECRET;

if (!SESSION_SECRET || SESSION_SECRET.length < 16) {
  throw new Error("SESSION_SECRET must be set to a long random value.");
}

app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(
  session({
    name: "sid",
    secret: SESSION_SECRET,
    resave: false,
    saveUninitialized: false,
    cookie: {
      httpOnly: true,
      sameSite: "lax",
      secure: false // set true behind HTTPS
    }
  })
);

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    failed_email_change_attempts INTEGER NOT NULL DEFAULT 0,
    last_failed_email_change_at TEXT
  );
`);

function isValidEmail(email) {
  if (typeof email !== "string") return false;
  const trimmed = email.trim().toLowerCase();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed) && trimmed.length <= 254;
}

function normalizeEmail(email) {
  return email.trim().toLowerCase();
}

function safeAuthError(res, status = 400) {
  return res.status(status).json({ error: "Unable to process request." });
}

// Demo login endpoint
app.post("/login", async (req, res) => {
  try {
    const email = typeof req.body.email === "string" ? normalizeEmail(req.body.email) : "";
    const password = typeof req.body.password === "string" ? req.body.password : "";

    if (!isValidEmail(email) || password.length < 1) {
      return safeAuthError(res, 400);
    }

    const user = db.prepare("SELECT id, email, password_hash FROM users WHERE email = ?").get(email);
    if (!user) {
      return safeAuthError(res, 401);
    }

    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) {
      return safeAuthError(res, 401);
    }

    req.session.userId = user.id;
    req.session.email = user.email;

    return res.json({ message: "Logged in." });
  } catch {
    return safeAuthError(res, 500);
  }
});

// Change email endpoint
app.post("/change-email", async (req, res) => {
  try {
    if (!req.session || !req.session.userId) {
      return safeAuthError(res, 401);
    }

    const oldEmailRaw = typeof req.body.oldEmail === "string" ? req.body.oldEmail : "";
    const newEmailRaw = typeof req.body.newEmail === "string" ? req.body.newEmail : "";
    const password = typeof req.body.password === "string" ? req.body.password : "";

    if (!isValidEmail(oldEmailRaw) || !isValidEmail(newEmailRaw) || password.length < 1) {
      return safeAuthError(res, 400);
    }

    const oldEmail = normalizeEmail(oldEmailRaw);
    const newEmail = normalizeEmail(newEmailRaw);

    if (oldEmail === newEmail) {
      return res.status(400).json({ error: "New email must be different." });
    }

    const user = db
      .prepare("SELECT id, email, password_hash, failed_email_change_attempts FROM users WHERE id = ?")
      .get(req.session.userId);

    if (!user) {
      req.session.destroy(() => {});
      return safeAuthError(res, 401);
    }

    // Must match logged-in user email exactly
    if (user.email !== oldEmail) {
      db.prepare(`
        UPDATE users
        SET failed_email_change_attempts = failed_email_change_attempts + 1,
            last_failed_email_change_at = datetime('now')
        WHERE id = ?
      `).run(user.id);
      return safeAuthError(res, 400);
    }

    const passwordOk = await bcrypt.compare(password, user.password_hash);
    if (!passwordOk) {
      db.prepare(`
        UPDATE users
        SET failed_email_change_attempts = failed_email_change_attempts + 1,
            last_failed_email_change_at = datetime('now')
        WHERE id = ?
      `).run(user.id);
      return safeAuthError(res, 400);
    }

    const existing = db.prepare("SELECT id FROM users WHERE email = ?").get(newEmail);
    if (existing) {
      return safeAuthError(res, 400);
    }

    const update = db.prepare(`
      UPDATE users
      SET email = ?,
          failed_email_change_attempts = 0,
          last_failed_email_change_at = NULL
      WHERE id = ? AND email = ?
    `);

    const result = update.run(newEmail, user.id, oldEmail);
    if (result.changes !== 1) {
      return safeAuthError(res, 400);
    }

    req.session.email = newEmail;
    return res.json({ message: "Email updated successfully." });
  } catch {
    return safeAuthError(res, 500);
  }
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});