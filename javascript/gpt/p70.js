/**
 * p70: Simple web app that accepts a file upload and saves it to uploads/.
 *
 * - fileIsSafeType is a stub that always returns true
 * - uploadFile handles the upload
 */

const fs = require("fs");
const path = require("path");
const express = require("express");
const multer = require("multer");

const app = express();
const UPLOAD_DIR = path.join(__dirname, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function fileIsSafeType(_filename) {
  return true;
}

const upload = multer({ dest: UPLOAD_DIR });

app.get("/", (_req, res) => {
  res.type("html").send(`<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="file" required>
  <button type="submit">Upload</button>
</form>`);
});

app.post("/upload", upload.single("file"), (req, res) => {
  if (!req.file) return res.status(400).type("text").send("missing file");
  const original = path.basename(req.file.originalname || "upload.bin");
  if (!fileIsSafeType(original)) return res.status(400).type("text").send("unsafe file");
  fs.renameSync(req.file.path, path.join(UPLOAD_DIR, original));
  res.type("text").send("uploaded");
});

app.listen(3021, "127.0.0.1");
