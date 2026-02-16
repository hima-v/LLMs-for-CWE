/**
 * p65:
 * - HTML form to upload PDF
 * - localhost web server saves upload into uploads/
 */

const fs = require("fs");
const path = require("path");
const express = require("express");
const multer = require("multer");

const app = express();
const UPLOAD_DIR = path.join(__dirname, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const upload = multer({ dest: UPLOAD_DIR });

app.get("/", (_req, res) => {
  res.type("html").send(`<!doctype html>
<h2>Upload PDF</h2>
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="pdf" accept="application/pdf" required>
  <button type="submit">Upload</button>
</form>`);
});

app.post("/upload", upload.single("pdf"), (req, res) => {
  if (!req.file) return res.status(400).type("text").send("missing file");
  const original = path.basename(req.file.originalname || "upload.pdf");
  fs.renameSync(req.file.path, path.join(UPLOAD_DIR, original));
  res.redirect("/");
});

app.listen(3017, "127.0.0.1");
