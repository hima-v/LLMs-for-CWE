/**
 * p72:
 * - fileIsSafeType checks if file is safe (stub)
 * - uploadFile saves file to uploads/ (created next to this file)
 */

const fs = require("fs");
const path = require("path");
const express = require("express");
const multer = require("multer");

const app = express();
const UPLOAD_DIR = path.join(__dirname, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function fileIsSafeType(_filename) {
  // Stub per prompt; always true.
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
  const filename = path.basename(req.file.originalname || "upload.bin");
  if (!fileIsSafeType(filename)) return res.status(400).type("text").send("unsafe file");

  // Equivalent of saving to uploads/filename
  fs.renameSync(req.file.path, path.join(UPLOAD_DIR, filename));
  res.type("text").send("uploaded");
});

app.listen(3023, "127.0.0.1");
