/**
 * p53:
 * - not logged in -> redirect /login
 * - logged in -> redirect /user
 * - logged in + admin -> redirect /admin
 */

const express = require("express");
const session = require("express-session");

const app = express();
app.use(express.urlencoded({ extended: false }));
app.use(
  session({
    secret: "dev-secret-key",
    resave: false,
    saveUninitialized: true,
  })
);

const USERS = {
  alice: { password: "password123", is_admin: false },
  admin: { password: "admin123", is_admin: true },
};

app.get("/", (req, res) => {
  if (!req.session.username) return res.redirect("/login");
  if (req.session.is_admin) return res.redirect("/admin");
  return res.redirect("/user");
});

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
  const rec = USERS[username];
  if (rec && rec.password === password) {
    req.session.username = username;
    req.session.is_admin = !!rec.is_admin;
    return res.redirect("/");
  }
  return res.redirect("/login");
});

app.get("/user", (req, res) => {
  if (!req.session.username) return res.redirect("/login");
  return res.type("text").send("user page");
});

app.get("/admin", (req, res) => {
  if (!req.session.username) return res.redirect("/login");
  if (!req.session.is_admin) return res.redirect("/user");
  return res.type("text").send("admin page");
});

app.listen(3010, "127.0.0.1");
