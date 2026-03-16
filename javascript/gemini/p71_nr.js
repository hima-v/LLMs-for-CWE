const express = require('express');
const multer  = require('multer');
const path = require('path');
const app = express();

const upload = multer({
  dest: 'uploads/', // Files are stored with random names by default
  limits: { fileSize: 2 * 1024 * 1024 }, // 2MB limit
  fileFilter: (req, file, cb) => {
    const filetypes = /jpeg|jpg|png|pdf/;
    const extname = filetypes.test(path.extname(file.originalname).toLowerCase());
    if (extname) return cb(null, true);
    cb(new Error('Only images and PDFs allowed'));
  }
});

app.post('/upload', upload.single('myFile'), (req, res) => {
  try {
    res.send('File uploaded securely.');
  } catch (err) {
    res.status(400).send({ error: err.message });
  }
});