// app.js
// npm install express multer
const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || path.join(__dirname, "uploads"));
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
const ALLOWED_EXTENSIONS = new Set([".png", ".jpg", ".jpeg", ".pdf", ".txt"]);
const ALLOWED_MIME_TYPES = new Set([
  "image/png",
  "image/jpeg",
  "application/pdf",
  "text/plain",
]);

function sanitizeFilename(filename) {
  const base = path.basename(filename || "upload");
  const cleaned = base.replace(/[^A-Za-z0-9._-]/g, "_").slice(0, 120);
  return cleaned || "upload";
}

function fileIsSafeType(filename, mimetype, buffer) {
  const ext = path.extname(filename).toLowerCase();
  if (!ALLOWED_EXTENSIONS.has(ext)) return false;
  if (!ALLOWED_MIME_TYPES.has(mimetype)) return false;

  if (ext === ".png") {
    const pngSig = Buffer.from([0x89, 0x50, 0x4e, 0x47]);
    if (!buffer.subarray(0, 4).equals(pngSig)) return false;
  }

  if (ext === ".jpg" || ext === ".jpeg") {
    if (!(buffer[0] === 0xff && buffer[1] === 0xd8)) return false;
  }

  if (ext === ".pdf") {
    if (buffer.subarray(0, 5).toString("ascii") !== "%PDF-") return false;
  }

  if (ext === ".txt") {
    if (buffer.includes(0x00)) return false;
  }

  return true;
}

function safeDestination(originalName) {
  const safe = sanitizeFilename(originalName);
  const ext = path.extname(safe).toLowerCase();
  const stem = path.basename(safe, ext).slice(0, 80);
  const finalName = `${stem}_${crypto.randomUUID()}${ext}`;
  const fullPath = path.resolve(path.join(UPLOAD_DIR, finalName));

  if (path.dirname(fullPath) !== UPLOAD_DIR) {
    throw new Error("Invalid destination path");
  }
  return { finalName, fullPath };
}

function uploadFile(file) {
  if (!file || !file.originalname || !file.buffer) {
    throw new Error("No file provided");
  }

  const safeName = sanitizeFilename(file.originalname);

  if (!fileIsSafeType(safeName, file.mimetype, file.buffer.subarray(0, 8192))) {
    throw new Error("Unsupported or unsafe file type");
  }

  const { finalName, fullPath } = safeDestination(safeName);

  const fd = fs.openSync(
    fullPath,
    fs.constants.O_WRONLY | fs.constants.O_CREAT | fs.constants.O_EXCL,
    0o600
  );

  try {
    fs.writeFileSync(fd, file.buffer);
    fs.closeSync(fd);
  } catch (err) {
    try {
      fs.closeSync(fd);
    } catch (_) {}
    try {
      fs.unlinkSync(fullPath);
    } catch (_) {}
    throw err;
  }

  return finalName;
}

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_FILE_SIZE, files: 1 },
});

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

app.post("/upload", upload.single("file"), (req, res) => {
  try {
    const saved = uploadFile(req.file);
    res.status(201).json({ ok: true, file: saved });
  } catch (err) {
    if (err && err.message && (
      err.message.includes("Unsupported") ||
      err.message.includes("No file") ||
      err.message.includes("Invalid")
    )) {
      return res.status(400).json({ ok: false, error: err.message });
    }
    res.status(500).json({ ok: false, error: "Upload failed" });
  }
});

app.use((err, _req, res, _next) => {
  if (err instanceof multer.MulterError) {
    return res.status(400).json({ ok: false, error: "Invalid upload request" });
  }
  res.status(500).json({ ok: false, error: "Upload failed" });
});

app.listen(3000, "127.0.0.1", () => {
  console.log("Server running on http://127.0.0.1:3000");
});