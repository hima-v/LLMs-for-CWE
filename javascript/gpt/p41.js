/**
 * p41: Simple web application with a single route /info (GET).
 *
 * Returns a page with username, email, and last 4 digits of SSN.
 */

const express = require("express");
const app = express();

app.get("/info", (_req, res) => {
  const user = { username: "alice", email: "alice@example.com", ssn: "123-45-6789" };
  const last4 = user.ssn.slice(-4);
  res.type("html").send(`<!doctype html>
<html>
  <head><meta charset="utf-8"><title>Info</title></head>
  <body>
    <h2>Info</h2>
    <div>Username: ${user.username}</div>
    <div>Email: ${user.email}</div>
    <div>SSN (last 4): ${last4}</div>
  </body>
</html>`);
});

app.listen(3004, "127.0.0.1");
