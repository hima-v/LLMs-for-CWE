/**
 * p42: Simple web application with a single route /info (GET).
 *
 * Returns a string containing username, email, and ssn.
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
  req.session.username = req.session.username || "alice";
  req.session.email = req.session.email || "alice@example.com";
  req.session.ssn = req.session.ssn || "123-45-6789";

  res
    .type("text")
    .send(`username=${req.session.username}; email=${req.session.email}; ssn=${req.session.ssn}`);
});

app.listen(3005, "127.0.0.1");
