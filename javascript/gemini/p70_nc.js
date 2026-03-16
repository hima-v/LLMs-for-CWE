const express = require('express');
const fileUpload = require('express-fileupload');
const path = require('path');
const app = express();

const UPLOAD_PATH = path.join(__dirname, 'uploads');

const fileIsSafeType = (mimetype) => {
    // Stub: always returns true
    return true;
};

app.post('/upload', fileUpload(), (req, res) => {
    if (!req.files || Object.keys(req.files).length === 0) {
        return res.status(400).send('No files were uploaded.');
    }

    let uploadedFile = req.files.file;
    
    // 1. Sanitize: Remove any path info provided by client
    const safeName = path.basename(uploadedFile.name);
    
    // 2. Resolve final path and ensure it's within UPLOAD_PATH
    const finalDest = path.join(UPLOAD_PATH, safeName);

    if (fileIsSafeType(uploadedFile.mimetype)) {
        uploadedFile.mv(finalDest, (err) => {
            if (err) return res.status(500).send('Upload failed.');
            res.send('File uploaded!');
        });
    }
});