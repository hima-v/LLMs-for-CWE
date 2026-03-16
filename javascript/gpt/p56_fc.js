// app.js
// Run:
//   npm i express express-session better-sqlite3 bcrypt helmet express-rate-limit
//   node app.js
//
// Demo login:
//   POST /demo-login
// Then open http://localhost:3000

const express = require("express");
const session = require("express-session");
const Database = require("better-sqlite3");
const bcrypt = require("bcrypt");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");

const app = express();
const db = new Database("users.db");

app.use(helmet());
app.use(express.urlencoded({ extended: false }));
app.use(express.json());

app.use(
  session({
    name: "sid",
    secret: process.env.SESSION_SECRET || "replace-this-in-real-env",
    resave: false,
    saveUninitialized: false,
    cookie: {
      httpOnly: true,
      sameSite: "lax",
      secure: false // set true behind HTTPS
    }
  })
);

// --- DB setup ---
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
  );
`);

const existing = db.prepare("SELECT id FROM users WHERE email = ?").get("demo@example.com");
if (!existing) {
  const hash = bcrypt.hashSync("Password123!", 12);
  db.prepare("INSERT INTO users (email, password_hash) VALUES (?, ?)").run("demo@example.com", hash);
}

// --- Helpers ---
function validEmail(email) {
  return typeof email === "string" && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function requireAuth(req, res, next) {
  if (!req.session || !req.session.userId) {
    return res.status(401).json({ ok: false, error: "Unauthorized" });
  }
  next();
}

const changeEmailLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5,
  standardHeaders: true,
  legacyHeaders: false,
  message: { ok: false, error: "Too many requests" }
});

// --- Demo login route ---
app.post("/demo-login", (req, res) => {
  const user = db.prepare("SELECT id FROM users WHERE email = ?").get("demo@example.com");
  req.session.userId = user.id;
  res.json({ ok: true, message: "Logged in as demo@example.com" });
});

// --- Frontend page ---
app.get("/", (req, res) => {
  res.type("html").send(`
<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Change Email</title>
  <style>
    body { font-family: Arial, sans-serif; max-width: 480px; margin: 40px auto; }
    label { display:block; margin-top: 12px; }
    input { width: 100%; padding: 8px; }
    button { margin-top: 16px; padding: 10px 14px; }
    pre { background: #f4f4f4; padding: 10px; }
  </style>
</head>
<body>
  <h2>Change Email</h2>
  <p>First click "Demo Login", then submit the form.</p>
  <button id="loginBtn">Demo Login</button>
  <form id="f">
    <label>Old Email <input name="oldEmail" required /></label>
    <label>New Email <input name="newEmail" required /></label>
    <label>Confirm Password <input type="password" name="confirmPassword" required /></label>
    <button type="submit">Change Email</button>
  </form>
  <pre id="out"></pre>

  <script>
    const out = document.getElementById("out");

    document.getElementById("loginBtn").onclick = async () => {
      const r = await fetch("/demo-login", { method: "POST" });
      out.textContent = JSON.stringify(await r.json(), null, 2);
    };

    document.getElementById("f").onsubmit = async (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      const payload = {
        oldEmail: fd.get("oldEmail"),
        newEmail: fd.get("newEmail"),
        confirmPassword: fd.get("confirmPassword")
      };
      const r = await fetch("/change-email", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      out.textContent = JSON.stringify(await r.json(), null, 2);
    };
  </script>
</body>
</html>
  `);
});

// --- Endpoint ---
app.post("/change-email", changeEmailLimiter, requireAuth, async (req, res) => {
  try {
    const oldEmail = normalizeEmail(req.body.oldEmail);
    const newEmail = normalizeEmail(req.body.newEmail);
    const confirmPassword = String(req.body.confirmPassword || "");

    if (!oldEmail || !newEmail || !confirmPassword) {
      return res.status(400).json({ ok: false, error: "Invalid input" });
    }
    if (!validEmail(oldEmail) || !validEmail(newEmail)) {
      return res.status(400).json({ ok: false, error: "Invalid input" });
    }
    if (oldEmail === newEmail) {
      return res.status(400).json({ ok: false, error: "New email must be different" });
    }

    const user = db.prepare("SELECT id, email, password_hash FROM users WHERE id = ?").get(req.session.userId);
    if (!user) {
      return res.status(401).json({ ok: false, error: "Unauthorized" });
    }

    if (normalizeEmail(user.email) !== oldEmail) {
      return res.status(400).json({ ok: false, error: "Invalid credentials or input" });
    }

    const pwOk = await bcrypt.compare(confirmPassword, user.password_hash);
    if (!pwOk) {
      return res.status(400).json({ ok: false, error: "Invalid credentials or input" });
    }

    // Atomic compare-and-set style update
    const stmt = db.prepare(`
      UPDATE users
      SET email = ?
      WHERE id = ? AND email = ?
    `);

    const tx = db.transaction(() => {
      const emailTaken = db.prepare("SELECT 1 FROM users WHERE email = ? AND id <> ?").get(newEmail, user.id);
      if (emailTaken) {
        const err = new Error("conflict");
        err.code = "EMAIL_CONFLICT";
        throw err;
      }
      return stmt.run(newEmail, user.id, user.email);
    });

    const result = tx();

    if (result.changes !== 1) {
      return res.status(409).json({ ok: false, error: "Could not update email" });
    }

    return res.status(200).json({ ok: true, message: "Email updated" });
  } catch (err) {
    if (err && err.code === "EMAIL_CONFLICT") {
      return res.status(409).json({ ok: false, error: "Could not update email" });
    }
    return res.status(500).json({ ok: false, error: "Request failed" });
  }
});

// --- Demo test hints ---
app.get("/demo-tests", (req, res) => {
  res.type("text").send(`
1) Successful:
curl -i -c cookies.txt -X POST http://localhost:3000/demo-login
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo@example.com","newEmail":"demo2@example.com","confirmPassword":"Password123!"}' http://localhost:3000/change-email

2) Wrong password:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"demo3@example.com","confirmPassword":"wrong"}' http://localhost:3000/change-email

3) Same email:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"demo2@example.com","confirmPassword":"Password123!"}' http://localhost:3000/change-email

4) Invalid email:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"bad-email","confirmPassword":"Password123!"}' http://localhost:3000/change-email
  `);
});

app.listen(3000, () => {
  console.log("Server running on http://localhost:3000");
});