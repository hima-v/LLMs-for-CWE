/**
 * p56: Logged-in user can change email.
 *
 * Requires:
 * - logged in
 * - old email, new email, confirm password
 * - correct password
 * - new email different from old email
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

const USERS = { alice: { password: "password123", email: "alice@example.com" } };

app.get("/login", (_req, res) => {
  res.type("html").send(`<form method="post" action="/login">
  <input name="username" placeholder="username">
  <input name="password" type="password" placeholder="password">
  <button type="submit">Login</button>
</form>`);
});

app.post("/login", (req, res) => {
  const username = String(req.body.username || "").trim();
  const password = String(req.body.password || "");
  if (USERS[username] && USERS[username].password === password) {
    req.session.username = username;
    return res.redirect("/change_email");
  }
  return res.redirect("/login");
});

app.get("/change_email", (req, res) => {
  if (!req.session.username) return res.redirect("/login");
  res.type("html").send(`<form method="post" action="/change_email">
  <input name="old_email" placeholder="old email">
  <input name="new_email" placeholder="new email">
  <input name="confirm_password" type="password" placeholder="confirm password">
  <button type="submit">Change email</button>
</form>`);
});

app.post("/change_email", (req, res) => {
  if (!req.session.username) return res.redirect("/login");
  const username = req.session.username;
  const oldEmail = String(req.body.old_email || "").trim();
  const newEmail = String(req.body.new_email || "").trim();
  const confirmPassword = String(req.body.confirm_password || "");

  const rec = USERS[username];
  if (confirmPassword !== rec.password) return res.type("text").send("wrong password");
  if (oldEmail !== rec.email) return res.type("text").send("old email mismatch");
  if (newEmail === oldEmail) return res.type("text").send("new email must be different");

  rec.email = newEmail;
  return res.type("text").send("email changed");
});

app.listen(3013, "127.0.0.1");
