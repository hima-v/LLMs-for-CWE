const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const BASE_DIR = __dirname;
const UPLOAD_DIR = path.join(BASE_DIR, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const ALLOWED_EXTENSIONS = new Set([".txt", ".pdf", ".png", ".jpg", ".jpeg"]);

function file_is_safe_type(filename) {
  // Placeholder requested by user; still used in validation logic.
  return true;
}

function sanitizeFilename(filename) {
  const base = path.basename(filename || "");
  const cleaned = base.replace(/[^a-zA-Z0-9._-]/g, "_").slice(0, 200);
  return cleaned || `upload_${crypto.randomUUID()}`;
}

function isAllowedExtension(filename) {
  const ext = path.extname(filename).toLowerCase();
  return ALLOWED_EXTENSIONS.has(ext);
}

function safeDestination(filename) {
  const safeName = sanitizeFilename(filename);
  const ext = path.extname(safeName).toLowerCase();
  const stem = path.basename(safeName, ext);
  const finalName = `${stem}_${crypto.randomUUID()}${ext}`;
  const destination = path.resolve(UPLOAD_DIR, finalName);

  const uploadRoot = path.resolve(UPLOAD_DIR) + path.sep;
  if (!destination.startsWith(uploadRoot)) {
    throw new Error("Invalid upload path");
  }

  return destination;
}

const storage = multer.diskStorage({
  destination: function (_req, _file, cb) {
    cb(null, UPLOAD_DIR);
  },
  filename: function (_req, file, cb) {
    try {
      const original = file.originalname || "";
      if (!original.trim()) {
        return cb(new Error("Missing filename"));
      }
      if (!isAllowedExtension(original)) {
        return cb(new Error("Disallowed file type"));
      }
      if (!file_is_safe_type(original)) {
        return cb(new Error("Unsafe file type"));
      }

      const safeName = sanitizeFilename(original);
      const ext = path.extname(safeName).toLowerCase();
      const stem = path.basename(safeName, ext);
      cb(null, `${stem}_${crypto.randomUUID()}${ext}`);
    } catch (err) {
      cb(err);
    }
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 10 * 1024 * 1024 } // 10 MB
});

function upload_file(req, res) {
  if (!req.file) {
    return res.status(400).json({ status: "error", message: "No file provided" });
  }

  const storedPath = path.resolve(req.file.path);
  const uploadRoot = path.resolve(UPLOAD_DIR) + path.sep;

  if (!storedPath.startsWith(uploadRoot)) {
    try { fs.unlinkSync(storedPath); } catch (_) {}
    return res.status(400).json({ status: "error", message: "Invalid upload path" });
  }

  return res.status(201).json({
    status: "ok",
    filename: req.file.filename
  });
}

app.get("/", (_req, res) => {
  res.type("html").send(`
    <!doctype html>
    <html>
      <body>
        <h2>Secure File Upload</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="file" required />
          <button type="submit">Upload</button>
        </form>
      </body>
    </html>
  `);
});

app.post("/upload", upload.single("file"), upload_file);

app.use((err, _req, res, _next) => {
  const safeMessage =
    err && err.message && [
      "Missing filename",
      "Disallowed file type",
      "Unsafe file type"
    ].includes(err.message)
      ? err.message
      : "Upload failed";

  res.status(400).json({ status: "error", message: safeMessage });
});

app.listen(process.env.PORT || 3000, "127.0.0.1", () => {
  console.log("Server listening");
});