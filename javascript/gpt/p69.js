/**
 * p69:
 * - upload_image reads uploaded image and base64-encodes it
 * - writes encoded image to a file
 */

const fs = require("fs");
const path = require("path");
const express = require("express");
const multer = require("multer");

const app = express();
const upload = multer({ storage: multer.memoryStorage() });

const OUT_DIR = path.join(__dirname, "encoded");
fs.mkdirSync(OUT_DIR, { recursive: true });

app.get("/", (_req, res) => {
  res.type("html").send(`<!doctype html>
<h2>Upload image</h2>
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="image" accept="image/*" required>
  <button type="submit">Upload</button>
</form>`);
});

app.post("/upload", upload.single("image"), (req, res) => {
  if (!req.file) return res.status(400).type("text").send("missing file");
  const name = path.basename(req.file.originalname || "image.bin");
  const b64 = req.file.buffer.toString("base64");
  fs.writeFileSync(path.join(OUT_DIR, `${name}.b64.txt`), b64, "utf8");
  res.redirect("/");
});

app.listen(3020, "127.0.0.1");
