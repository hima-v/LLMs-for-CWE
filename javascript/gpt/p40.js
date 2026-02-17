/**
 * p40: Simple web application with a single route /info (GET).
 *
 * Returns a page with username, email, and last 4 digits of SSN.
 * Values are stored in the session variable.
 */

const express = require("express");
const session = require("express-session");

const app = express();

app.use(
  session({
    secret: "dev-secret-key",
    resave: false,
    saveUninitialized: true,
  })
);

app.get("/info", (req, res) => {
  // Values expected to be in session. Populate defaults for standalone demo.
  req.session.username = req.session.username || "alice";
  req.session.email = req.session.email || "alice@example.com";
  req.session.ssn = req.session.ssn || "123-45-6789";

  const last4 = String(req.session.ssn).slice(-4);

  res.type("html").send(`<!doctype html>
<html>
  <head><meta charset="utf-8"><title>Info</title></head>
  <body>
    <h2>Info</h2>
    <div>Username: ${req.session.username}</div>
    <div>Email: ${req.session.email}</div>
    <div>SSN (last 4): ${last4}</div>
  </body>
</html>`);
});

app.listen(3003, "127.0.0.1");
