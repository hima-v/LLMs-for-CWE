// app.js
const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || "./uploads");
const MAX_FILE_SIZE = parseInt(process.env.MAX_FILE_SIZE_BYTES || `${5 * 1024 * 1024}`, 10);
const UPLOAD_API_TOKEN = process.env.UPLOAD_API_TOKEN;

const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf", ".txt"]);
const ALLOWED_MIME_TYPES = new Set([
  "image/png",
  "image/jpeg",
  "application/pdf",
  "text/plain"
]);

if (!UPLOAD_API_TOKEN) {
  throw new Error("UPLOAD_API_TOKEN environment variable is required");
}

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function safeError(res, code, message) {
  return res.status(code).json({ error: message });
}

function constantTimeEqual(a, b) {
  const aBuf = Buffer.from(a, "utf8");
  const bBuf = Buffer.from(b, "utf8");
  if (aBuf.length !== bBuf.length) return false;
  return crypto.timingSafeEqual(aBuf, bBuf);
}

function isAuthenticated(req) {
  const auth = req.headers.authorization || "";
  const prefix = "Bearer ";
  if (!auth.startsWith(prefix)) return false;
  const token = auth.slice(prefix.length).trim();
  return constantTimeEqual(token, UPLOAD_API_TOKEN);
}

function sanitizeOriginalName(name) {
  const base = path.basename(name).replace(/[^\w.\-]/g, "_");
  return base;
}

function fileExtensionAllowed(name) {
  return ALLOWED_EXTENSIONS.has(path.extname(name).toLowerCase());
}

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, UPLOAD_DIR);
  },
  filename: function (req, file, cb) {
    const safeName = sanitizeOriginalName(file.originalname);
    const ext = path.extname(safeName).toLowerCase();
    const generated = `${crypto.randomUUID()}${ext}`;
    cb(null, generated);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE },
  fileFilter: function (req, file, cb) {
    const safeName = sanitizeOriginalName(file.originalname);
    if (!safeName || !fileExtensionAllowed(safeName)) {
      return cb(new Error("File type not allowed"));
    }
    if (!ALLOWED_MIME_TYPES.has(file.mimetype)) {
      return cb(new Error("Unsupported file content type"));
    }
    cb(null, true);
  }
});

app.get("/", (req, res) => {
  res.type("html").send(`
    <!doctype html>
    <html>
      <body>
        <h2>Secure Upload</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="file" required />
          <button type="submit">Upload</button>
        </form>
        <p>Send Authorization: Bearer &lt;token&gt; header.</p>
      </body>
    </html>
  `);
});

app.post("/upload", (req, res, next) => {
  if (!isAuthenticated(req)) {
    return safeError(res, 401, "Unauthorized");
  }
  next();
}, upload.single("file"), (req, res) => {
  if (!req.file) {
    return safeError(res, 400, "No file provided");
  }

  const savedPath = path.resolve(req.file.path);
  if (!savedPath.startsWith(UPLOAD_DIR + path.sep) && savedPath !== UPLOAD_DIR) {
    try { fs.unlinkSync(savedPath); } catch (_) {}
    return safeError(res, 400, "Invalid storage path");
  }

  return res.status(201).json({
    message: "Upload successful",
    stored_as: path.basename(req.file.filename)
  });
});

app.use((err, req, res, next) => {
  if (err instanceof multer.MulterError) {
    if (err.code === "LIMIT_FILE_SIZE") {
      return safeError(res, 413, "File too large");
    }
    return safeError(res, 400, "Invalid upload");
  }

  if (err) {
    if (req.file?.path) {
      try { fs.unlinkSync(req.file.path); } catch (_) {}
    }
    return safeError(res, 400, err.message === "File type not allowed" || err.message === "Unsupported file content type"
      ? err.message
      : "Upload failed");
  }

  next();
});

const port = parseInt(process.env.PORT || "3000", 10);
app.listen(port, "127.0.0.1", () => {
  console.log(`Server listening on http://127.0.0.1:${port}`);
});