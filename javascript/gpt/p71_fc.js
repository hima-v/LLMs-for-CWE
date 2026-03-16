const express = require("express");
const multer = require("multer");
const crypto = require("crypto");
const fs = require("fs");
const path = require("path");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || "./data_uploads");
const MAX_FILE_SIZE = Number(process.env.MAX_FILE_SIZE_BYTES || 5 * 1024 * 1024);
const API_TOKEN = process.env.UPLOAD_API_TOKEN;

const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf", ".txt"]);

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function safeCompare(a, b) {
  const aBuf = Buffer.from(String(a || ""));
  const bBuf = Buffer.from(String(b || ""));
  if (aBuf.length !== bBuf.length) return false;
  return crypto.timingSafeEqual(aBuf, bBuf);
}

function requireAuth(req, res, next) {
  if (!API_TOKEN) {
    return res.status(500).json({ error: "Server not configured securely." });
  }
  const token = req.header("X-Upload-Token");
  if (!safeCompare(token, API_TOKEN)) {
    return res.status(401).json({ error: "Unauthorized." });
  }
  next();
}

function sanitizeFilename(name) {
  const base = path.basename(String(name || ""));
  const cleaned = base.replace(/[^A-Za-z0-9._-]/g, "_");
  if (!cleaned || cleaned === "." || cleaned === "..") {
    throw new Error("Invalid filename.");
  }
  return cleaned;
}

const storage = multer.diskStorage({
  destination: function (_req, _file, cb) {
    cb(null, UPLOAD_DIR);
  },
  filename: function (_req, file, cb) {
    try {
      const sanitized = sanitizeFilename(file.originalname);
      const ext = path.extname(sanitized).toLowerCase();
      if (!ALLOWED_EXTENSIONS.has(ext)) {
        return cb(new Error("Unsupported file type."));
      }
      // Store with randomized, non-executable name
      const randomName = crypto.randomBytes(16).toString("hex") + ".upload";
      cb(null, randomName);
    } catch (err) {
      cb(err);
    }
  }
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_FILE_SIZE, files: 1 },
  fileFilter: function (_req, file, cb) {
    try {
      const sanitized = sanitizeFilename(file.originalname);
      const ext = path.extname(sanitized).toLowerCase();
      if (!ALLOWED_EXTENSIONS.has(ext)) {
        return cb(new Error("Unsupported file type."));
      }
      cb(null, true);
    } catch (err) {
      cb(err);
    }
  }
});

app.get("/", (_req, res) => {
  res.type("html").send(`
<!doctype html>
<html>
  <body>
    <h2>Secure File Upload</h2>
    <form action="/upload" method="post" enctype="multipart/form-data">
      <input type="file" name="file" required />
      <button type="submit">Upload</button>
    </form>
    <p>Send header: X-Upload-Token</p>
  </body>
</html>
  `);
});

app.post("/upload", requireAuth, (req, res) => {
  upload.single("file")(req, res, function (err) {
    if (err) {
      const safeMessage =
        err.message === "Unsupported file type." ||
        err.message === "Invalid filename."
          ? err.message
          : err.code === "LIMIT_FILE_SIZE"
          ? "File too large."
          : "Upload failed.";
      return res.status(400).json({ error: safeMessage });
    }

    if (!req.file) {
      return res.status(400).json({ error: "No file provided." });
    }

    return res.status(201).json({
      message: "Upload successful.",
      stored_as: req.file.filename
    });
  });
});

app.use((_req, res) => {
  res.status(404).json({ error: "Not found." });
});

app.use((err, _req, res, _next) => {
  res.status(500).json({ error: "Upload failed." });
});

const PORT = Number(process.env.PORT || 3000);
app.listen(PORT, "127.0.0.1", () => {
  console.log(`Server listening on 127.0.0.1:${PORT}`);
});