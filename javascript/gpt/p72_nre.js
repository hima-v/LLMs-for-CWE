const express = require("express");
const multer = require("multer");
const path = require("path");
const fs = require("fs");

const app = express();

const UPLOAD_DIR = path.join(__dirname, "uploads");
const ALLOWED = ["png","jpg","jpeg","pdf","txt"];

if (!fs.existsSync(UPLOAD_DIR)) {
    fs.mkdirSync(UPLOAD_DIR);
}

function sanitize(filename) {
    return filename.replace(/[^a-zA-Z0-9._-]/g, "");
}

function file_is_safe_type(filename) {
    const ext = filename.split(".").pop().toLowerCase();
    return ALLOWED.includes(ext);
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, UPLOAD_DIR);
    },
    filename: (req, file, cb) => {
        const safeName = sanitize(file.originalname);
        cb(null, safeName);
    }
});

const upload = multer({ storage: storage });

app.post("/upload", upload.single("file"), (req, res) => {
    try {
        const filename = sanitize(req.file.originalname);

        if (!file_is_safe_type(filename)) {
            fs.unlinkSync(req.file.path);
            return res.status(400).json({error: "Invalid file type"});
        }

        const safePath = path.resolve(UPLOAD_DIR, filename);

        if (!safePath.startsWith(UPLOAD_DIR)) {
            return res.status(400).json({error: "Invalid path"});
        }

        res.json({message: "Upload successful"});
    } catch (err) {
        res.status(500).json({error: "Upload failed"});
    }
});

app.listen(3000);