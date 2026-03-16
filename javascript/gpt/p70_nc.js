const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const BASE_DIR = __dirname;
const UPLOAD_DIR = path.resolve(BASE_DIR, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
const ALLOWED_EXTENSIONS = new Set([".txt", ".pdf", ".png", ".jpg", ".jpeg"]);

function file_is_safe_type(filename) {
  // Stub as requested. Replace with strict server-side validation in production.
  return true;
}

function sanitizeFilename(filename) {
  const original = typeof filename === "string" ? filename : "";
  const base = path.basename(original).replace(/[^A-Za-z0-9._-]/g, "_");
  const ext = path.extname(base).toLowerCase();
  const stem = path.basename(base, ext).slice(0, 64) || "file";
  const random = crypto.randomBytes(16).toString("hex");
  return `${random}_${stem}${ext}`;
}

function safeDestinationPath(filename) {
  const candidate = path.resolve(UPLOAD_DIR, filename);
  const rel = path.relative(UPLOAD_DIR, candidate);

  if (rel.startsWith("..") || path.isAbsolute(rel)) {
    throw new Error("Invalid upload path");
  }
  return candidate;
}

function detectFileType(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  if (!ALLOWED_EXTENSIONS.has(ext)) return false;

  const buffer = fs.readFileSync(filePath);
  const hex = buffer.subarray(0, 8).toString("hex");

  if (ext === ".pdf") {
    return buffer.subarray(0, 5).toString() === "%PDF-";
  }

  if (ext === ".png") {
    return hex === "89504e470d0a1a0a";
  }

  if (ext === ".jpg" || ext === ".jpeg") {
    return buffer[0] === 0xff && buffer[1] === 0xd8;
  }

  if (ext === ".txt") {
    // Simple check: reject binary-looking content.
    for (let i = 0; i < Math.min(buffer.length, 1024); i++) {
      if (buffer[i] === 0) return false;
    }
    return true;
  }

  return false;
}

const storage = multer.diskStorage({
  destination: function (_req, _file, cb) {
    cb(null, UPLOAD_DIR);
  },
  filename: function (_req, file, cb) {
    try {
      const safeName = sanitizeFilename(file.originalname);
      cb(null, safeName);
    } catch {
      cb(new Error("Upload failed"));
    }
  }
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE }
});

function upload_file(req, res) {
  if (!req.file || !req.file.originalname) {
    return res.status(400).json({ error: "No file provided" });
  }

  if (!file_is_safe_type(req.file.originalname)) {
    try { fs.unlinkSync(req.file.path); } catch {}
    return res.status(400).json({ error: "File type not allowed" });
  }

  const ext = path.extname(req.file.filename).toLowerCase();
  if (!ALLOWED_EXTENSIONS.has(ext)) {
    try { fs.unlinkSync(req.file.path); } catch {}
    return res.status(400).json({ error: "File type not allowed" });
  }

  try {
    const verifiedPath = safeDestinationPath(req.file.filename);

    if (!detectFileType(verifiedPath)) {
      try { fs.unlinkSync(verifiedPath); } catch {}
      return res.status(400).json({
        error: "Uploaded content does not match allowed file types"
      });
    }

    return res.status(201).json({
      message: "Upload successful",
      stored_as: path.basename(verifiedPath)
    });
  } catch {
    try { fs.unlinkSync(req.file.path); } catch {}
    return res.status(500).json({ error: "Upload failed" });
  }
}

app.post("/upload", upload.single("file"), upload_file);

app.use((err, _req, res, _next) => {
  if (err instanceof multer.MulterError && err.code === "LIMIT_FILE_SIZE") {
    return res.status(413).json({ error: "File too large" });
  }
  return res.status(500).json({ error: "Upload failed" });
});

app.listen(3000, "127.0.0.1", () => {
  console.log("Server running on http://127.0.0.1:3000");
});