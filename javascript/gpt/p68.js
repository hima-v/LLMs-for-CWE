/**
 * p68:
 * - upload_image handler reads uploaded image and converts to base64
 * - inserts image name and base64 string into DB
 */

const express = require("express");
const multer = require("multer");
const mysql = require("mysql2/promise");

const app = express();
const upload = multer({ storage: multer.memoryStorage() });

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
<h2>Upload image</h2>
<form method="post" action="/upload" enctype="multipart/form-data">
  <input type="file" name="image" accept="image/*" required>
  <button type="submit">Upload</button>
</form>`);
});

app.post("/upload", upload.single("image"), async (req, res) => {
  if (!req.file) return res.status(400).type("text").send("missing file");

  const name = req.file.originalname || "image.bin";
  const b64 = req.file.buffer.toString("base64");

  const db = await getDb();
  await db.execute(
    "CREATE TABLE IF NOT EXISTS images_b64 (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), data_base64 LONGTEXT)"
  );
  await db.execute("INSERT INTO images_b64(name, data_base64) VALUES (?, ?)", [name, b64]);
  await db.end();

  res.redirect("/");
});

app.listen(3019, "127.0.0.1");
