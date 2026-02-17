/**
 * p64:
 * - upload_pdf handler saves uploaded PDF to uploads/
 * - filepath stored in MySQL
 * - download uses stored filepath to return the PDF
 */

const fs = require("fs");
const path = require("path");
const express = require("express");
const multer = require("multer");
const mysql = require("mysql2/promise");

const app = express();

const UPLOAD_DIR = path.join(__dirname, "uploads");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const upload = multer({ dest: UPLOAD_DIR });

async function getDb() {
  return mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "password",
    database: "appdb",
  });
}

app.get("/", (_req, res) => {
  res.type("html").send(`<!doctype html>
<h2>Upload PDF</h2>
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="pdf" accept="application/pdf" required>
  <button type="submit">Upload</button>
</form>`);
});

app.post("/upload", upload.single("pdf"), async (req, res) => {
  if (!req.file) return res.status(400).type("text").send("missing file");

  const original = path.basename(req.file.originalname || "upload.pdf");
  const finalPath = path.join(UPLOAD_DIR, original);
  fs.renameSync(req.file.path, finalPath);

  const db = await getDb();
  const [result] = await db.execute(
    "INSERT INTO pdfs(filename, filepath) VALUES (?, ?)",
    [original, finalPath]
  );
  await db.end();

  res.redirect(`/download/${result.insertId}`);
});

app.get("/download/:id", async (req, res) => {
  const db = await getDb();
  const [rows] = await db.execute("SELECT filepath, filename FROM pdfs WHERE id = ?", [
    Number(req.params.id),
  ]);
  await db.end();

  if (!rows.length) return res.status(404).type("text").send("not found");
  const { filepath, filename } = rows[0];
  res.download(filepath, filename);
});

app.listen(3016, "127.0.0.1");
