// app.js
const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || "./uploads");
const MAX_FILE_SIZE = Number(process.env.MAX_FILE_SIZE || 5 * 1024 * 1024); // 5 MB
const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf"]);

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function generateSafeFilename(originalName) {
  const ext = path.extname(originalName).toLowerCase();
  return crypto.randomUUID() + ext;
}

function isAllowedExtension(filename) {
  return ALLOWED_EXTENSIONS.has(path.extname(filename).toLowerCase());
}

function fileTypeLooksSafe(buffer, ext) {
  if (ext === ".pdf") return buffer.slice(0, 4).toString() === "%PDF";
  if (ext === ".png") {
    return buffer.length >= 8 &&
      buffer[0] === 0x89 &&
      buffer[1] === 0x50 &&
      buffer[2] === 0x4e &&
      buffer[3] === 0x47 &&
      buffer[4] === 0x0d &&
      buffer[5] === 0x0a &&
      buffer[6] === 0x1a &&
      buffer[7] === 0x0a;
  }
  if (ext === ".jpg" || ext === ".jpeg") {
    return buffer.length >= 3 &&
      buffer[0] === 0xff &&
      buffer[1] === 0xd8 &&
      buffer[2] === 0xff;
  }
  return false;
}

const storage = multer.memoryStorage();

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE },
  fileFilter: (req, file, cb) => {
    if (!file.originalname || !isAllowedExtension(file.originalname)) {
      return cb(new Error("Unsupported file type"));
    }
    cb(null, true);
  }
});

app.post("/upload", upload.single("file"), (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No file provided" });
    }

    const ext = path.extname(req.file.originalname).toLowerCase();
    if (!fileTypeLooksSafe(req.file.buffer, ext)) {
      return res.status(400).json({ error: "File content does not match allowed type" });
    }

    const safeName = generateSafeFilename(req.file.originalname);
    const destination = path.resolve(UPLOAD_DIR, safeName);

    // Ensure destination stays inside upload dir
    if (!destination.startsWith(UPLOAD_DIR + path.sep)) {
      return res.status(400).json({ error: "Invalid upload path" });
    }

    fs.writeFileSync(destination, req.file.buffer, { mode: 0o600, flag: "wx" });
    // "wx" prevents overwriting existing files

    return res.status(201).json({
      message: "Upload successful",
      stored_as: safeName
    });
  } catch (err) {
    return res.status(500).json({ error: "Upload failed" });
  }
});

app.use((err, req, res, next) => {
  if (err instanceof multer.MulterError && err.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({ error: "File too large" });
  }
  if (err) {
    return res.status(400).json({ error: "Invalid upload" });
  }
  next();
});

app.listen(3000, "127.0.0.1", () => {
  console.log("Server listening on http://127.0.0.1:3000");
});