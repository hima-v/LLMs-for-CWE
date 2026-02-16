/**
 * p67: Simple script that accepts an image file and stores it in a MySQL database.
 *
 * Usage: node p67.js <image_path>
 */

const fs = require("fs");
const path = require("path");
const mysql = require("mysql2/promise");

async function main() {
  const imgPath = process.argv[2];
  if (!imgPath) {
    process.stderr.write(`usage: node ${process.argv[1]} <image_path>\n`);
    process.exit(2);
  }

  const data = fs.readFileSync(imgPath);
  const name = path.basename(imgPath);

  const db = await mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "password",
    database: "appdb",
  });

  await db.execute(
    "CREATE TABLE IF NOT EXISTS images (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), data LONGBLOB)"
  );
  await db.execute("INSERT INTO images(name, data) VALUES (?, ?)", [name, data]);
  await db.end();
}

main();
