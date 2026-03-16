// secure_upload.js
// Minimal secure file upload example using Node.js + Express + Multer.
// Run:
//   npm install express multer
//   UPLOAD_API_KEY=change-me node secure_upload.js
//
// Upload with:
//   curl -F "file=@example.png" -H "X-API-Key: change-me" http://127.0.0.1:3000/upload

const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();
const BASE_DIR = __dirname;
const UPLOAD_DIR = path.join(BASE_DIR, "uploads");
const MAX_FILE_SIZE = 5 * 1024 * 1024;
const API_KEY = process.env.UPLOAD_API_KEY || "change-me";
const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf", ".txt"]);

fs.mkdirSync(UPLOAD_DIR, { recursive: true, mode: 0o750 });

function sanitizeFilename(filename) {
  const base = path.basename(filename || "");
  const safe = base.replace(/[^A-Za-z0-9._-]/g, "_");
  if (!safe || safe === "." || safe === "..") {
    throw new Error("Invalid filename");
  }
  return safe;
}

function fileIsSafeType(originalname, buffer) {
  const ext = path.extname(originalname).toLowerCase();
  if (!ALLOWED_EXTENSIONS.has(ext)) return false;

  if (ext === ".pdf") return buffer.slice(0, 5).toString() === "%PDF-";
  if (ext === ".png") return buffer.slice(0, 8).equals(Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a]));
  if (ext === ".jpg" || ext === ".jpeg") return buffer[0] === 0xff && buffer[1] === 0xd8 && buffer[2] === 0xff;
  if (ext === ".txt") return !buffer.includes(0x00);

  return false;
}

function safeDestinationPath(filename) {
  const safeName = sanitizeFilename(filename);
  const finalName = `${crypto.randomUUID()}_${safeName}`;
  const dest = path.resolve(UPLOAD_DIR, finalName);
  const root = path.resolve(UPLOAD_DIR);

  if (!dest.startsWith(root + path.sep)) {
    throw new Error("Unsafe destination path");
  }
  return dest;
}

const storage = multer.memoryStorage();
const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE },
});

app.post("/upload", upload.single("file"), (req, res) => {
  try {
    if ((req.header("X-API-Key") || "") !== API_KEY) {
      return res.status(401).json({ error: "Unauthorized" });
    }

    if (!req.file) {
      return res.status(400).json({ error: "No file provided" });
    }

    if (!fileIsSafeType(req.file.originalname, req.file.buffer)) {
      return res.status(400).json({ error: "Disallowed or invalid file type" });
    }

    const dest = safeDestinationPath(req.file.originalname);
    fs.writeFileSync(dest, req.file.buffer, { mode: 0o640, flag: "wx" });

    return res.status(201).json({
      message: "Upload successful",
      stored_as: path.basename(dest),
    });
  } catch (_err) {
    return res.status(500).json({ error: "Upload failed" });
  }
});

app.use((err, _req, res, _next) => {
  if (err instanceof multer.MulterError && err.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({ error: "File too large" });
  }
  return res.status(400).json({ error: "Invalid upload request" });
});

app.listen(3000, () => {
  console.log("Server listening on http://127.0.0.1:3000");
});