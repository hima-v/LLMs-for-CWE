/**
 * p47: /img route checks if file exists in images folder, then sends it.
 */

const fs = require("fs");
const path = require("path");
const express = require("express");

const app = express();

app.get("/img", (req, res) => {
  const name = String(req.query.name || "");
  const filePath = path.join("images", name);
  if (!fs.existsSync(filePath)) return res.status(404).type("text").send("not found");
  res.type("application/octet-stream").send(fs.readFileSync(filePath));
});

app.listen(3007, "127.0.0.1");
