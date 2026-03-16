// app.js
// Run:
//   npm init -y
//   npm install express express-session better-sqlite3
//   SESSION_SECRET="replace-this-with-a-long-random-secret" node app.js

const express = require("express");
const session = require("express-session");
const crypto = require("crypto");
const Database = require("better-sqlite3");

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

const SESSION_SECRET = process.env.SESSION_SECRET;
if (!SESSION_SECRET || SESSION_SECRET.length < 16) {
  console.error("Missing or weak SESSION_SECRET.");
  process.exit(1);
}

app.use(
  session({
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

const db = new Database(":memory:");
db.exec(`
  CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL
  );
`);

function hashPassword(password, salt) {
  return crypto.pbkdf2Sync(password, salt, 210000, 32, "sha256").toString("hex");
}

function createPasswordRecord(password) {
  const salt = crypto.randomBytes(16).toString("hex");
  const hash = hashPassword(password, salt);
  return { salt, hash };
}

function safeEqualHex(aHex, bHex) {
  const a = Buffer.from(aHex, "hex");
  const b = Buffer.from(bHex, "hex");
  if (a.length !== b.length) return false;
  return crypto.timingSafeEqual(a, b);
}

function verifyPassword(password, salt, expectedHash) {
  const actualHash = hashPassword(password, salt);
  return safeEqualHex(actualHash, expectedHash);
}

function isValidEmail(email) {
  if (typeof email !== "string") return false;
  const trimmed = email.trim().toLowerCase();
  if (trimmed.length < 3 || trimmed.length > 254) return false;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed);
}

function normalizeEmail(email) {
  return email.trim().toLowerCase();
}

// Demo seed user
const demoPassword = process.env.DEMO_PASSWORD || "ChangeMe123!";
const seeded = createPasswordRecord(demoPassword);
db.prepare(
  "INSERT INTO users (email, password_hash, password_salt) VALUES (?, ?, ?)"
).run("user@example.com", seeded.hash, seeded.salt);

function requireAuth(req, res, next) {
  if (!req.session || !req.session.userId) {
    return res.status(401).json({ error: "Unauthorized." });
  }
  next();
}

app.post("/login", (req, res) => {
  try {
    const email = normalizeEmail(String(req.body.email || ""));
    const password = String(req.body.password || "");

    if (!isValidEmail(email) || password.length < 1) {
      return res.status(400).json({ error: "Invalid request." });
    }

    const user = db.prepare("SELECT * FROM users WHERE email = ?").get(email);
    if (!user) {
      return res.status(401).json({ error: "Invalid credentials." });
    }

    const ok = verifyPassword(password, user.password_salt, user.password_hash);
    if (!ok) {
      return res.status(401).json({ error: "Invalid credentials." });
    }

    req.session.userId = user.id;
    return res.json({ message: "Logged in." });
  } catch {
    return res.status(500).json({ error: "Request failed." });
  }
});

app.post("/change-email", requireAuth, (req, res) => {
  try {
    const oldEmail = normalizeEmail(String(req.body.oldEmail || ""));
    const newEmail = normalizeEmail(String(req.body.newEmail || ""));
    const password = String(req.body.password || "");

    if (!isValidEmail(oldEmail) || !isValidEmail(newEmail) || password.length < 1) {
      return res.status(400).json({ error: "Invalid request." });
    }

    if (oldEmail === newEmail) {
      return res.status(400).json({ error: "New email must be different." });
    }

    const user = db.prepare("SELECT * FROM users WHERE id = ?").get(req.session.userId);
    if (!user) {
      req.session.destroy(() => {});
      return res.status(401).json({ error: "Unauthorized." });
    }

    if (user.email !== oldEmail) {
      return res.status(400).json({ error: "Could not update email." });
    }

    const ok = verifyPassword(password, user.password_salt, user.password_hash);
    if (!ok) {
      return res.status(400).json({ error: "Could not update email." });
    }

    const tx = db.transaction(() => {
      const existing = db.prepare("SELECT id FROM users WHERE email = ?").get(newEmail);
      if (existing) {
        throw new Error("EMAIL_TAKEN");
      }

      const result = db
        .prepare("UPDATE users SET email = ? WHERE id = ? AND email = ?")
        .run(newEmail, user.id, oldEmail);

      if (result.changes !== 1) {
        throw new Error("UPDATE_FAILED");
      }
    });

    try {
      tx();
    } catch {
      return res.status(400).json({ error: "Could not update email." });
    }

    return res.json({ message: "Email updated successfully." });
  } catch {
    return res.status(500).json({ error: "Request failed." });
  }
});

app.post("/logout", requireAuth, (req, res) => {
  req.session.destroy(() => {
    res.json({ message: "Logged out." });
  });
});

app.listen(3000, () => {
  console.log("Server running on http://localhost:3000");
  console.log("Demo login:");
  console.log("  email: user@example.com");
  console.log("  password:", demoPassword);
});