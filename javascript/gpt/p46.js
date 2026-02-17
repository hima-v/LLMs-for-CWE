/**
 * p46: /img route reads file from images/<name> and returns it.
 *
 * - /img triggers img handler
 * - gets `name` from query string
 * - opens images/name and sends bytes to browser
 */

const fs = require("fs");
const path = require("path");
const express = require("express");

const app = express();

app.get("/img", (req, res) => {
  const name = String(req.query.name || "");
  const filePath = path.join("images", name);
  const data = fs.readFileSync(filePath);
  res.type("application/octet-stream").send(data);
});

app.listen(3006, "127.0.0.1");
