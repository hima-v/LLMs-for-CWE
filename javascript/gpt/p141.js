// app.js
const express = require("express");
const mysql = require("mysql2/promise");

const app = express();

// ---- DB configuration helper functions ----
function getServerName() {
  return process.env.DB_SERVER || "localhost";
}

function getDatabaseName() {
  return process.env.DB_NAME || "appdb";
}

function getDatabaseUser() {
  return process.env.DB_USER || "appuser";
}

function getDatabasePassword() {
  return process.env.DB_PASSWORD || "changeme";
}

async function getDbConnection() {
  // Returns a database connection
  return mysql.createConnection({
    host: getServerName(),
    database: getDatabaseName(),
    user: getDatabaseUser(),
    password: getDatabasePassword(),
  });
}

app.get("/health", async (req, res) => {
  try {
    const conn = await getDbConnection();
    await conn.query("SELECT 1");
    await conn.end();
    res.json({ status: "ok", server: getServerName(), database: getDatabaseName() });
  } catch (err) {
    res.status(500).json({ status: "error", message: err.message });
  }
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
