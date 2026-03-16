// app.js
// Run:
//   npm install express express-session better-sqlite3 bcrypt helmet
//   SESSION_SECRET="replace_me_with_a_long_random_secret" node app.js
//
// Notes:
// - No hardcoded passwords or secrets
// - Uses server-side session auth
// - Generic/safe error messages
// - Atomic email update using user id + old email condition

const express = require("express");
const session = require("express-session");
const Database = require("better-sqlite3");
const bcrypt = require("bcrypt");
const helmet = require("helmet");

const app = express();
const db = new Database("auth_demo.db");

const PORT = process.env.PORT || 3000;
const SESSION_SECRET = process.env.SESSION_SECRET;
if (!SESSION_SECRET || SESSION_SECRET.length < 16) {
  throw new Error("Set SESSION_SECRET to a long random value.");
}

app.use(helmet({ contentSecurityPolicy: false }));
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

// ---- DB init ----
db.exec(`
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
`);

function isValidEmail(email) {
  if (typeof email !== "string") return false;
  const value = email.trim().toLowerCase();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) && value.length <= 254;
}

function normalizeEmail(email) {
  return email.trim().toLowerCase();
}

function safeFail(res, status = 400, message = "Request could not be completed.") {
  return res.status(status).json({ ok: false, message });
}

function requireAuth(req, res, next) {
  if (!req.session || !req.session.userId) {
    return safeFail(res, 401, "Authentication required.");
  }
  next();
}

app.get("/", (req, res) => {
  res.type("html").send(`
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Login + Change Email</title></head>
<body>
  <h2>Register</h2>
  <form method="post" action="/register">
    <input name="email" type="email" placeholder="email" required />
    <input name="password" type="password" placeholder="password" required />
    <button type="submit">Register</button>
  </form>

  <h2>Login</h2>
  <form method="post" action="/login">
    <input name="email" type="email" placeholder="email" required />
    <input name="password" type="password" placeholder="password" required />
    <button type="submit">Login</button>
  </form>

  <h2>Change Email</h2>
  <form method="post" action="/change-email">
    <input name="oldEmail" type="email" placeholder="old email" required />
    <input name="newEmail" type="email" placeholder="new email" required />
    <input name="password" type="password" placeholder="current password" required />
    <button type="submit">Change Email</button>
  </form>

  <form method="post" action="/logout">
    <button type="submit">Logout</button>
  </form>
</body>
</html>
  `);
});

app.post("/register", async (req, res) => {
  try {
    const email = normalizeEmail(String(req.body.email || ""));
    const password = String(req.body.password || "");

    if (!isValidEmail(email) || password.length < 8 || password.length > 128) {
      return safeFail(res, 400, "Invalid input.");
    }

    const exists = db.prepare("SELECT id FROM users WHERE email = ?").get(email);
    if (exists) {
      return safeFail(res, 400, "Request could not be completed.");
    }

    const passwordHash = await bcrypt.hash(password, 12);
    const info = db
      .prepare("INSERT INTO users (email, password_hash) VALUES (?, ?)")
      .run(email, passwordHash);

    req.session.userId = info.lastInsertRowid;
    return res.json({ ok: true, message: "Registered and logged in." });
  } catch {
    return safeFail(res, 500, "Server error.");
  }
});

app.post("/login", async (req, res) => {
  try {
    const email = normalizeEmail(String(req.body.email || ""));
    const password = String(req.body.password || "");

    if (!isValidEmail(email) || !password) {
      return safeFail(res, 400, "Invalid input.");
    }

    const user = db.prepare("SELECT id, password_hash FROM users WHERE email = ?").get(email);
    if (!user) {
      return safeFail(res, 401, "Invalid credentials.");
    }

    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) {
      return safeFail(res, 401, "Invalid credentials.");
    }

    req.session.userId = user.id;
    return res.json({ ok: true, message: "Logged in." });
  } catch {
    return safeFail(res, 500, "Server error.");
  }
});

app.post("/change-email", requireAuth, async (req, res) => {
  try {
    const oldEmail = normalizeEmail(String(req.body.oldEmail || ""));
    const newEmail = normalizeEmail(String(req.body.newEmail || ""));
    const password = String(req.body.password || "");

    if (!isValidEmail(oldEmail) || !isValidEmail(newEmail) || !password) {
      return safeFail(res, 400, "Invalid input.");
    }
    if (oldEmail === newEmail) {
      return safeFail(res, 400, "New email must be different.");
    }

    const user = db
      .prepare("SELECT id, email, password_hash FROM users WHERE id = ?")
      .get(req.session.userId);

    if (!user) {
      req.session.destroy(() => {});
      return safeFail(res, 401, "Authentication required.");
    }

    if (user.email !== oldEmail) {
      return safeFail(res, 400, "Request could not be completed.");
    }

    const passwordOk = await bcrypt.compare(password, user.password_hash);
    if (!passwordOk) {
      return safeFail(res, 401, "Invalid credentials.");
    }

    const emailTaken = db.prepare("SELECT id FROM users WHERE email = ?").get(newEmail);
    if (emailTaken) {
      return safeFail(res, 400, "Request could not be completed.");
    }

    const update = db
      .prepare("UPDATE users SET email = ? WHERE id = ? AND email = ?")
      .run(newEmail, user.id, oldEmail);

    if (update.changes !== 1) {
      return safeFail(res, 400, "Request could not be completed.");
    }

    return res.json({ ok: true, message: "Email updated." });
  } catch {
    return safeFail(res, 500, "Server error.");
  }
});

app.post("/logout", (req, res) => {
  req.session.destroy(() => {
    res.json({ ok: true, message: "Logged out." });
  });
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});