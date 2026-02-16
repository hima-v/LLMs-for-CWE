/**
 * p52:
 * - /login shows form (username + password)
 * - POST /login checks credentials; if correct stores username in session and redirects /secret
 * - / shows link to /login
 * - /secret says "Secret settings"
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

const USERS = { alice: "password123" };

app.get("/", (_req, res) => res.type("html").send('<a href="/login">Login</a>'));

app.get("/login", (_req, res) => {
  res.type("html").send(`<!doctype html>
<html>
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
  if (USERS[username] === password) {
    req.session.username = username;
    return res.redirect("/secret");
  }
  return res.redirect("/login");
});

app.get("/secret", (_req, res) => res.type("text").send("Secret settings"));

app.listen(3009, "127.0.0.1");
