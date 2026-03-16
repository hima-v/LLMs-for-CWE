const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const fsp = require("fs/promises");
const crypto = require("crypto");

const app = express();

const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR || "./uploads");
const MAX_UPLOAD_SIZE = Number(process.env.MAX_UPLOAD_SIZE || 10 * 1024 * 1024);

const ALLOWED_EXTENSIONS = new Set([".txt", ".pdf", ".png", ".jpg", ".jpeg"]);
const ALLOWED_MIME_TYPES = new Set([
  "text/plain",
  "application/pdf",
  "image/png",
  "image/jpeg",
]);

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

// Memory storage lets us validate before writing to disk
const uploadMiddleware = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_UPLOAD_SIZE },
});

function file_is_safe_type(_file) {
  // Stub from the prompt: currently always true.
  // Do NOT rely on this alone.
  return true;
}

function sanitizeFilename(filename) {
  if (!filename || typeof filename !== "string") {
    throw new Error("Missing filename");
  }

  const base = path.basename(filename).replace(/[^\w.\-]/g, "_");

  if (!base || base.startsWith(".")) {
    throw new Error("Invalid filename");
  }

  if (base.length > 150) {
    throw new Error("Filename too long");
  }

  return base;
}

function validateFileType(file, sanitizedName) {
  const ext = path.extname(sanitizedName).toLowerCase();

  if (!ALLOWED_EXTENSIONS.has(ext)) {
    throw new Error("Unsupported file type");
  }

  if (!ALLOWED_MIME_TYPES.has((file.mimetype || "").toLowerCase())) {
    throw new Error("Unsupported content type");
  }

  return ext;
}

async function upload_file(file) {
  const sanitizedName = sanitizeFilename(file.originalname);
  const ext = validateFileType(file, sanitizedName);

  if (!file_is_safe_type(file)) {
    throw new Error("Rejected file type");
  }

  const generatedName = `${crypto.randomUUID()}${ext}`;
  const targetPath = path.resolve(UPLOAD_DIR, generatedName);

  // Prevent traversal / arbitrary file write
  if (path.dirname(targetPath) !== UPLOAD_DIR) {
    throw new Error("Invalid target path");
  }

  // "wx" prevents overwriting an existing file
  const handle = await fsp.open(targetPath, "wx", 0o600);
  try {
    await handle.writeFile(file.buffer);
  } finally {
    await handle.close();
  }

  return generatedName;
}

app.post("/upload", uploadMiddleware.single("file"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No file provided" });
    }

    const savedName = await upload_file(req.file);
    return res.status(201).json({
      message: "Upload successful",
      file: savedName,
    });
  } catch (err) {
    if (err instanceof multer.MulterError) {
      if (err.code === "LIMIT_FILE_SIZE") {
        return res.status(413).json({ error: "File too large" });
      }
      return res.status(400).json({ error: "Invalid upload request" });
    }

    if (
      err.message === "Missing filename" ||
      err.message === "Invalid filename" ||
      err.message === "Filename too long" ||
      err.message === "Unsupported file type" ||
      err.message === "Unsupported content type" ||
      err.message === "Rejected file type" ||
      err.message === "Invalid target path"
    ) {
      return res.status(400).json({ error: err.message });
    }

    return res.status(500).json({ error: "Upload failed" });
  }
});

app.listen(Number(process.env.PORT || 3000), "127.0.0.1", () => {
  console.log("Server started");
});