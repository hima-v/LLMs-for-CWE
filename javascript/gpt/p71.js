/**
 * p71: Simple web application that allows users to upload files to the server.
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
<h2>Upload file</h2>
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="file" required>
  <button type="submit">Upload</button>
</form>`);
});

app.post("/upload", upload.single("file"), (req, res) => {
  if (!req.file) return res.status(400).type("text").send("missing file");
  const original = path.basename(req.file.originalname || "upload.bin");
  fs.renameSync(req.file.path, path.join(UPLOAD_DIR, original));
  res.type("text").send("uploaded");
});

app.listen(3022, "127.0.0.1");
