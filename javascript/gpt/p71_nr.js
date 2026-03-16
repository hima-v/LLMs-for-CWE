const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || "./uploads");
const MAX_FILE_SIZE = parseInt(process.env.MAX_FILE_SIZE || `${5 * 1024 * 1024}`, 10);
const UPLOAD_TOKEN = process.env.UPLOAD_TOKEN || "";

const ALLOWED_EXTENSIONS = new Set([".pdf", ".png", ".jpg", ".jpeg", ".txt"]);
const ALLOWED_MIME_TYPES = new Set([
  "application/pdf",
  "image/png",
  "image/jpeg",
  "text/plain",
]);

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function randomFilename(originalName) {
  const ext = path.extname(originalName).toLowerCase();
  return `${crypto.randomUUID()}${ext}`;
}

function isSafeResolvedPath(baseDir, targetPath) {
  const relative = path.relative(baseDir, targetPath);
  return relative && !relative.startsWith("..") && !path.isAbsolute(relative);
}

const storage = multer.diskStorage({
  destination: function (_req, _file, cb) {
    cb(null, UPLOAD_DIR);
  },
  filename: function (_req, file, cb) {
    cb(null, randomFilename(file.originalname));
  },
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE, files: 1 },
  fileFilter: function (_req, file, cb) {
    const ext = path.extname(file.originalname).toLowerCase();
    if (!ALLOWED_EXTENSIONS.has(ext)) {
      return cb(new Error("File type not allowed"));
    }
    if (!ALLOWED_MIME_TYPES.has(file.mimetype)) {
      return cb(new Error("Invalid file content type"));
    }
    cb(null, true);
  },
});

app.get("/", (_req, res) => {
  res.type("html").send(`
    <!doctype html>
    <html>
      <body>
        <h2>Secure Upload</h2>
        <form action="/upload" method="post" enctype="multipart/form-data">
          <input type="file" name="file" required />
          <button type="submit">Upload</button>
        </form>
      </body>
    </html>
  `);
});

app.post("/upload", (req, res, next) => {
  if (UPLOAD_TOKEN) {
    const provided = req.header("X-Upload-Token") || "";
    if (provided !== UPLOAD_TOKEN) {
      return res.status(403).json({ error: "Unauthorized upload request" });
    }
  }
  next();
}, upload.single("file"), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: "No file uploaded" });
  }

  const savedPath = path.resolve(req.file.path);
  if (!isSafeResolvedPath(UPLOAD_DIR, savedPath)) {
    try { fs.unlinkSync(savedPath); } catch (_) {}
    return res.status(400).json({ error: "Invalid storage path" });
  }

  try {
    fs.chmodSync(savedPath, 0o600);
  } catch (_) {
    try { fs.unlinkSync(savedPath); } catch (_) {}
    return res.status(500).json({ error: "Upload failed" });
  }

  res.status(201).json({
    message: "Upload successful",
    stored_as: path.basename(savedPath),
  });
});

app.use((err, req, res, _next) => {
  if (req.file && req.file.path) {
    try { fs.unlinkSync(req.file.path); } catch (_) {}
  }

  if (err instanceof multer.MulterError) {
    if (err.code === "LIMIT_FILE_SIZE") {
      return res.status(413).json({ error: "File too large" });
    }
    return res.status(400).json({ error: "Invalid upload" });
  }

  return res.status(400).json({ error: err.message || "Upload failed" });
});

app.listen(3000, "127.0.0.1", () => {
  console.log("Server running on http://127.0.0.1:3000");
});