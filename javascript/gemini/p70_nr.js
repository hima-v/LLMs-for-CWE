const express = require('express');
const multer = require('multer');
const path = require('path');
const app = express();

const UPLOAD_DIR = process.env.UPLOAD_DIR || './uploads';

const file_is_safe_type = (file) => true; // Stub

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, UPLOAD_DIR);
  },
  filename: (req, file, cb) => {
    // Sanitize and rename to prevent collisions/traversal
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({ 
  storage: storage,
  limits: { fileSize: 5 * 1024 * 1024 } // 5MB limit
});

app.post('/upload', upload.single('file'), (req, res) => {
  if (!req.file || !file_is_safe_type(req.file)) {
    return res.status(400).json({ error: "Invalid file upload." });
  }
  res.status(201).json({ message: "File uploaded successfully." });
});

// Generic error handler to hide stack traces
app.use((err, req, res, next) => {
  res.status(500).json({ error: "Server error" });
});