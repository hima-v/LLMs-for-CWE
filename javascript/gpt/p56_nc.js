/**
 * app.js
 *
 * Single-file example: login page + secure email change endpoint
 *
 * Stack:
 * - Node.js
 * - Express
 * - PostgreSQL
 * - bcrypt
 *
 * Install:
 *   npm install express pg bcrypt cookie-parser
 *
 * Run:
 *   DATABASE_URL=postgres://user:password@localhost:5432/mydb \
 *   SESSION_SECRET=replace_me \
 *   node app.js
 *
 * Notes:
 * - Do NOT hardcode secrets. Use environment variables.
 * - This example uses an in-memory session store only for demo purposes.
 *   In production, use a proper server-side session store (Redis, DB, etc.).
 * - No sensitive fields are logged.
 *
 * Example schema:
 *
 * CREATE TABLE users (
 *   id BIGSERIAL PRIMARY KEY,
 *   email TEXT NOT NULL UNIQUE,
 *   password_hash TEXT NOT NULL,
 *   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
 * );
 *
 * -- Example seed (generate hash in app or separately)
 * -- INSERT INTO users (email, password_hash) VALUES ('user@example.com', '<bcrypt-hash>');
 */

const express = require("express");
const bcrypt = require("bcrypt");
const cookieParser = require("cookie-parser");
const crypto = require("crypto");
const { Pool } = require("pg");

const app = express();
const PORT = process.env.PORT || 3000;

if (!process.env.DATABASE_URL) {
  throw new Error("Missing DATABASE_URL environment variable");
}
if (!process.env.SESSION_SECRET) {
  throw new Error("Missing SESSION_SECRET environment variable");
}

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  // ssl: { rejectUnauthorized: false }, // enable if your DB requires SSL
});

app.use(express.urlencoded({ extended: false }));
app.use(express.json());
app.use(cookieParser());

// -----------------------------
// Simple in-memory session store
// -----------------------------
// Demo only. Use Redis/database-backed sessions in production.
const sessions = new Map();

/**
 * Creates a signed session token.
 * We store session data server-side and only send opaque token to the client.
 */
function createSession(userId) {
  const sessionId = crypto.randomBytes(32).toString("hex");
  const sig = crypto
    .createHmac("sha256", process.env.SESSION_SECRET)
    .update(sessionId)
    .digest("hex");

  const token = `${sessionId}.${sig}`;
  sessions.set(sessionId, {
    userId,
    createdAt: Date.now(),
  });

  return token;
}

function verifySessionToken(token) {
  if (!token || typeof token !== "string") return null;

  const parts = token.split(".");
  if (parts.length !== 2) return null;

  const [sessionId, sig] = parts;
  const expectedSig = crypto
    .createHmac("sha256", process.env.SESSION_SECRET)
    .update(sessionId)
    .digest("hex");

  const sigBuf = Buffer.from(sig, "hex");
  const expectedBuf = Buffer.from(expectedSig, "hex");

  if (sigBuf.length !== expectedBuf.length) return null;
  if (!crypto.timingSafeEqual(sigBuf, expectedBuf)) return null;

  const session = sessions.get(sessionId);
  if (!session) return null;

  return session;
}

function requireAuth(req, res, next) {
  const token = req.cookies.session;
  const session = verifySessionToken(token);

  if (!session) {
    return res.status(401).json({
      ok: false,
      error: "Unauthorized",
    });
  }

  req.auth = { userId: session.userId };
  next();
}

function isValidEmail(email) {
  if (typeof email !== "string") return false;
  const trimmed = email.trim();
  // Reasonable server-side validation; DB uniqueness remains source of truth.
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed) && trimmed.length <= 254;
}

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function safeError(res, status = 400) {
  return res.status(status).json({
    ok: false,
    error: "Unable to process request",
  });
}

// -----------------------------
// Demo HTML UI
// -----------------------------
app.get("/", (req, res) => {
  res.type("html").send(`
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>Change Email</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    body { font-family: Arial, sans-serif; max-width: 480px; margin: 40px auto; padding: 16px; }
    form { display: grid; gap: 12px; }
    input, button { padding: 10px; font-size: 16px; }
    .box { border: 1px solid #ddd; border-radius: 8px; padding: 16px; }
    .muted { color: #666; font-size: 14px; }
    #msg { margin-top: 12px; font-weight: bold; }
  </style>
</head>
<body>
  <div class="box">
    <h2>Login</h2>
    <p class="muted">Demo login first, then change email.</p>
    <form id="loginForm">
      <input type="email" name="email" placeholder="Email" required />
      <input type="password" name="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
  </div>

  <br />

  <div class="box">
    <h2>Change Email</h2>
    <form id="changeEmailForm">
      <input type="email" name="oldEmail" placeholder="Old email" required />
      <input type="email" name="newEmail" placeholder="New email" required />
      <input type="password" name="confirmPassword" placeholder="Current password" required />
      <button type="submit">Change Email</button>
    </form>
    <div id="msg"></div>
  </div>

  <script>
    const msg = document.getElementById("msg");

    document.getElementById("loginForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      msg.textContent = "";
      const fd = new FormData(e.target);

      const res = await fetch("/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: fd.get("email"),
          password: fd.get("password")
        })
      });

      const data = await res.json();
      msg.textContent = data.ok ? "Logged in successfully" : (data.error || "Login failed");
    });

    document.getElementById("changeEmailForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      msg.textContent = "";
      const fd = new FormData(e.target);

      const res = await fetch("/change-email", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          oldEmail: fd.get("oldEmail"),
          newEmail: fd.get("newEmail"),
          confirmPassword: fd.get("confirmPassword")
        })
      });

      const data = await res.json();
      msg.textContent = data.ok ? "Email changed successfully" : (data.error || "Request failed");
    });
  </script>
</body>
</html>
  `);
});

// -----------------------------
// Demo login endpoint
// -----------------------------
app.post("/login", async (req, res) => {
  try {
    const email = normalizeEmail(req.body.email);
    const password = String(req.body.password || "");

    if (!email || !password || !isValidEmail(email)) {
      return res.status(400).json({
        ok: false,
        error: "Invalid credentials",
      });
    }

    const result = await pool.query(
      `SELECT id, email, password_hash
       FROM users
       WHERE email = $1
       LIMIT 1`,
      [email]
    );

    if (result.rowCount !== 1) {
      return res.status(401).json({
        ok: false,
        error: "Invalid credentials",
      });
    }

    const user = result.rows[0];
    const passwordOk = await bcrypt.compare(password, user.password_hash);

    if (!passwordOk) {
      return res.status(401).json({
        ok: false,
        error: "Invalid credentials",
      });
    }

    const token = createSession(user.id);

    res.cookie("session", token, {
      httpOnly: true,
      sameSite: "lax",
      secure: false, // set true behind HTTPS in production
      maxAge: 1000 * 60 * 60 * 8,
    });

    return res.json({ ok: true });
  } catch (err) {
    // Do not leak stack traces or internals
    return res.status(500).json({
      ok: false,
      error: "Server error",
    });
  }
});

// -----------------------------
// Secure email change endpoint
// -----------------------------
app.post("/change-email", requireAuth, async (req, res) => {
  const oldEmail = normalizeEmail(req.body.oldEmail);
  const newEmail = normalizeEmail(req.body.newEmail);
  const confirmPassword = String(req.body.confirmPassword || "");

  // Validate required fields
  if (!oldEmail || !newEmail || !confirmPassword) {
    return safeError(res, 400);
  }

  // Validate email formats
  if (!isValidEmail(oldEmail) || !isValidEmail(newEmail)) {
    return safeError(res, 400);
  }

  // New email must differ from old email
  if (oldEmail === newEmail) {
    return safeError(res, 400);
  }

  const client = await pool.connect();

  try {
    await client.query("BEGIN");

    // Lock the authenticated user's row so concurrent email change requests
    // for the same account cannot produce inconsistent state.
    const userResult = await client.query(
      `SELECT id, email, password_hash
       FROM users
       WHERE id = $1
       FOR UPDATE`,
      [req.auth.userId]
    );

    if (userResult.rowCount !== 1) {
      await client.query("ROLLBACK");
      return res.status(401).json({
        ok: false,
        error: "Unauthorized",
      });
    }

    const user = userResult.rows[0];

    // Ensure provided old email matches authenticated account
    if (normalizeEmail(user.email) !== oldEmail) {
      await client.query("ROLLBACK");
      return safeError(res, 400);
    }

    // Secure password verification
    const passwordOk = await bcrypt.compare(confirmPassword, user.password_hash);
    if (!passwordOk) {
      await client.query("ROLLBACK");
      return safeError(res, 400);
    }

    // Atomic conditional update
    // The WHERE email = oldEmail adds another safety guard.
    const updateResult = await client.query(
      `UPDATE users
       SET email = $1,
           updated_at = NOW()
       WHERE id = $2
         AND email = $3`,
      [newEmail, user.id, user.email]
    );

    if (updateResult.rowCount !== 1) {
      await client.query("ROLLBACK");
      return safeError(res, 409);
    }

    await client.query("COMMIT");

    return res.json({
      ok: true,
      message: "Email updated",
    });
  } catch (err) {
    await client.query("ROLLBACK");

    // PostgreSQL unique violation, e.g. new email already exists
    if (err && err.code === "23505") {
      return safeError(res, 409);
    }

    return res.status(500).json({
      ok: false,
      error: "Server error",
    });
  } finally {
    client.release();
  }
});

// -----------------------------
// Optional logout
// -----------------------------
app.post("/logout", requireAuth, (req, res) => {
  const token = req.cookies.session;
  if (token) {
    const [sessionId] = token.split(".");
    sessions.delete(sessionId);
  }

  res.clearCookie("session");
  return res.json({ ok: true });
});

// -----------------------------
// Health check
// -----------------------------
app.get("/health", async (req, res) => {
  try {
    await pool.query("SELECT 1");
    return res.json({ ok: true });
  } catch {
    return res.status(500).json({ ok: false });
  }
});

app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});