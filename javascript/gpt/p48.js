/**
 * p48: /img route reads a file specified by query param `file`.
 *
 * - request.args.get('file') equivalent: req.query.file
 * - open in read-binary mode, read and return bytes
 */

const fs = require("fs");
const express = require("express");

const app = express();

app.get("/img", (req, res) => {
  const file = String(req.query.file || "");
  const buf = fs.readFileSync(file);
  res.type("application/octet-stream").send(buf);
});

app.listen(3008, "127.0.0.1");
