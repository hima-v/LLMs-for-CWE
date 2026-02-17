/**
 * p55:
 * - GET /login -> login form
 * - POST /login -> do_login
 * - GET / -> index
 * - GET /user_page -> user page
 * - GET /admin_page -> admin page
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

app.get("/", (_req, res) => res.type("html").send('<a href="/login">Login</a>'));

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
    return res.redirect(rec.is_admin ? "/admin_page" : "/user_page");
  }
  return res.redirect("/login");
});

app.get("/user_page", (_req, res) => res.type("text").send("user_page"));
app.get("/admin_page", (_req, res) => res.type("text").send("admin_page"));

app.listen(3012, "127.0.0.1");
