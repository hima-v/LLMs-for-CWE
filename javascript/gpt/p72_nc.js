// secure_upload.js
const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const BASE_DIR = __dirname;
const UPLOAD_DIR = path.join(BASE_DIR, "uploads");
const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf", ".txt"]);

function ensureUploadDir() {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true, mode: 0o750 });
}

function sanitizeFilename(filename) {
  const base = path.basename(filename || "");
  const ext = path.extname(base).toLowerCase();
  const stem = path.basename(base, ext).replace(/[^A-Za-z0-9._-]/g, "_").replace(/^[._-]+|[._-]+$/g, "");

  if (!stem || !ext) {
    throw new Error("Invalid filename");
  }

  const unique = crypto.randomUUID();
  return `${unique}_${stem}${ext}`;
}

function file_is_safe_type(filename) {
  const ext = path.extname(filename || "").toLowerCase();
  return ALLOWED_EXTENSIONS.has(ext);
}

function safeDestination(filename) {
  const dest = path.resolve(UPLOAD_DIR, filename);
  const root = path.resolve(UPLOAD_DIR);

  if (!(dest === root || dest.startsWith(root + path.sep))) {
    throw new Error("Unsafe path");
  }

  return dest;
}

ensureUploadDir();

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    try {
      ensureUploadDir();
      cb(null, UPLOAD_DIR);
    } catch {
      cb(new Error("Upload failed"));
    }
  },
  filename: function (req, file, cb) {
    try {
      if (!file_is_safe_type(file.originalname)) {
        return cb(new Error("File type not allowed"));
      }

      const safeName = sanitizeFilename(file.originalname);
      safeDestination(safeName); // validation step
      cb(null, safeName);
    } catch {
      cb(new Error("Invalid upload"));
    }
  }
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE },
  fileFilter: function (req, file, cb) {
    if (!file_is_safe_type(file.originalname)) {
      return cb(new Error("File type not allowed"));
    }
    cb(null, true);
  }
});

function upload_file(req, res) {
  upload.single("file")(req, res, function (err) {
    if (err) {
      return res.status(400).json({ error: "Invalid upload request" });
    }

    if (!req.file) {
      return res.status(400).json({ error: "No file provided" });
    }

    return res.status(201).json({
      message: "Upload successful",
      filename: req.file.filename
    });
  });
}

app.post("/upload", upload_file);

app.use((err, req, res, next) => {
  res.status(500).json({ error: "Upload failed" });
});

app.listen(3000, "127.0.0.1", () => {
  console.log("Server running on http://127.0.0.1:3000");
});