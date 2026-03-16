const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || "./uploads");
const MAX_FILE_SIZE = Number(process.env.MAX_FILE_SIZE || 5 * 1024 * 1024);

const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf", ".txt"]);
const ALLOWED_MIME = {
  ".png": new Set(["image/png"]),
  ".jpg": new Set(["image/jpeg"]),
  ".jpeg": new Set(["image/jpeg"]),
  ".pdf": new Set(["application/pdf"]),
  ".txt": new Set(["text/plain"]),
};

function ensureUploadDir() {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

function sanitizeFilename(filename) {
  const base = path.basename(filename).replace(/[^A-Za-z0-9._-]/g, "_");
  if (!base || !/^[A-Za-z0-9._-]+$/.test(base)) {
    throw new Error("Invalid filename");
  }
  return base;
}

function fileIsSafeType(filename, mimetype) {
  const ext = path.extname(filename).toLowerCase();
  return ALLOWED_EXTENSIONS.has(ext) && ALLOWED_MIME[ext] && ALLOWED_MIME[ext].has(mimetype);
}

ensureUploadDir();

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, UPLOAD_DIR);
  },
  filename: function (req, file, cb) {
    try {
      const cleaned = sanitizeFilename(file.originalname);
      const ext = path.extname(cleaned).toLowerCase();
      const randomName = crypto.randomUUID() + ext;
      const finalPath = path.resolve(UPLOAD_DIR, randomName);

      if (path.dirname(finalPath) !== UPLOAD_DIR) {
        return cb(new Error("Unsafe path"));
      }

      cb(null, randomName);
    } catch {
      cb(new Error("Invalid filename"));
    }
  },
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE },
  fileFilter: function (req, file, cb) {
    try {
      if (!fileIsSafeType(file.originalname, file.mimetype)) {
        return cb(new Error("File type not allowed"));
      }
      cb(null, true);
    } catch {
      cb(new Error("Invalid upload"));
    }
  },
});

app.post("/upload", upload.single("file"), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: "No file provided" });
  }
  return res.status(201).json({ message: "Upload successful" });
});

app.use((err, req, res, next) => {
  if (err instanceof multer.MulterError) {
    if (err.code === "LIMIT_FILE_SIZE") {
      return res.status(413).json({ error: "File too large" });
    }
    return res.status(400).json({ error: "Invalid upload" });
  }

  if (err) {
    return res.status(400).json({ error: "Upload failed" });
  }

  next();
});

app.listen(3000, "127.0.0.1", () => {
  console.log("Server listening on http://127.0.0.1:3000");
});