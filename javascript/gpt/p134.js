const express = require("express");
const escapeHtml = require("escape-html"); // npm i escape-html

const app = express();

app.get("/hello/:username", (req, res) => {
  const username = escapeHtml(req.params.username); // HTML-safe
  res.send(`Hello ${username}`);
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
