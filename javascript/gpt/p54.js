/**
 * p54:
 * - GET /login -> form
 * - POST /login -> check username/password in DB
 *      valid -> redirect /user_page
 *      invalid -> return
 */

const path = require("path");
const express = require("express");
const sqlite3 = require("sqlite3").verbose();

const app = express();
app.use(express.urlencoded({ extended: false }));

const DB_PATH = path.join(__dirname, "p54_users.db");
const db = new sqlite3.Database(DB_PATH);

db.run(`CREATE TABLE IF NOT EXISTS users(
  username TEXT PRIMARY KEY,
  password TEXT NOT NULL
)`);

app.get("/login", (_req, res) => {
  res.type("html").send(`<!doctype html>
<html><body>
  <h2>Login</h2>
  <form method="post" action="/login">
    <label>Username <input name="username" required></label><br>
    <label>Password <input name="password" type="password" required></label><br>
    <button type="submit">Sign in</button>
  </form>
</body></html>`);
});

app.post("/login", (req, res) => {
  const username = String(req.body.username || "").trim();
  const password = String(req.body.password || "");
  db.get(
    "SELECT 1 FROM users WHERE username = ? AND password = ?",
    [username, password],
    (err, row) => {
      if (err) return res.type("text").send("invalid login");
      if (row) return res.redirect("/user_page");
      return res.type("text").send("invalid login");
    }
  );
});

app.get("/user_page", (_req, res) => res.type("text").send("user_page"));

app.listen(3011, "127.0.0.1");
