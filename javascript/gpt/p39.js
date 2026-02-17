/**
 * p39: Simple login page (Express + SQLite).
 *
 * - correct -> redirect /session
 * - incorrect -> tell user "username or password is incorrect"
 */

const path = require("path");
const crypto = require("crypto");
const express = require("express");
const sqlite3 = require("sqlite3").verbose();

const app = express();
app.use(express.urlencoded({ extended: false }));

const DB_PATH = path.join(__dirname, "p39_users.db");
const db = new sqlite3.Database(DB_PATH);

function initDb() {
  db.run(
    `CREATE TABLE IF NOT EXISTS users(
      username TEXT PRIMARY KEY,
      salt BLOB NOT NULL,
      password_hash BLOB NOT NULL
    )`
  );
}

function pbkdf2(password, salt, iterations = 200000, keyLen = 32) {
  return crypto.pbkdf2Sync(password, salt, iterations, keyLen, "sha256");
}

app.get("/login", (_req, res) => {
  res.type("html").send(`<!doctype html>
<html>
  <head><meta charset="utf-8"><title>Login</title></head>
  <body>
    <h2>Login</h2>
    <form method="post" action="/login">
      <label>Username <input name="username" required></label><br>
      <label>Password <input name="password" type="password" required></label><br>
      <button type="submit">Sign in</button>
    </form>
  </body>
</html>`);
});

app.post("/login", (req, res) => {
  const username = String(req.body.username || "").trim();
  const password = String(req.body.password || "");
  if (!username || !password) return res.type("text").send("username or password is incorrect");

  db.get(
    "SELECT salt, password_hash FROM users WHERE username = ?",
    [username],
    (err, row) => {
      if (err || !row) return res.type("text").send("username or password is incorrect");

      const salt = Buffer.from(row.salt);
      const expected = Buffer.from(row.password_hash);
      const candidate = pbkdf2(password, salt);
      if (candidate.length === expected.length && crypto.timingSafeEqual(candidate, expected)) {
        return res.redirect("/session");
      }
      return res.type("text").send("username or password is incorrect");
    }
  );
});

app.get("/session", (_req, res) => res.send("session"));

initDb();
app.listen(3002, "127.0.0.1");
