// Node.js (sqlite3 + safe PHP exec)
"use strict";

const { spawn } = require("child_process");
const sqlite3 = require("sqlite3").verbose();

const DB_PATH = "/opt/app/app.db";
const PHP_BIN = "/usr/bin/php";
const PHP_SCRIPT = "/opt/app/unsub.php";
const PHP_TIMEOUT_MS = 5000;

const EMAIL_RE = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$/;

function normalizeEmail(email) {
  if (email === null || email === undefined) return null;
  const e = String(email).trim().toLowerCase();
  if (!e || e.length > 254) return null;
  if (e.includes("\u0000")) return null;
  if (!EMAIL_RE.test(e)) return null;
  const parts = e.split("@");
  if (parts.length !== 2) return null;
  if (parts[0].length > 64 || parts[1].length > 255) return null;
  return e;
}

function emailExists(email) {
  return new Promise((resolve) => {
    const db = new sqlite3.Database(DB_PATH, sqlite3.OPEN_READONLY, () => {
      db.get("SELECT 1 FROM subscribers WHERE email = ? LIMIT 1", [email], (err, row) => {
        db.close(() => resolve(!err && !!row));
      });
    });
    db.on("error", () => resolve(false));
  });
}

function runPhpUnsub(email) {
  return new Promise((resolve) => {
    const child = spawn(PHP_BIN, [PHP_SCRIPT, email], {
      stdio: ["ignore", "ignore", "ignore"],
      windowsHide: true,
    });

    let done = false;
    const timer = setTimeout(() => {
      if (done) return;
      done = true;
      try { child.kill("SIGKILL"); } catch (_) {}
      resolve(1);
    }, PHP_TIMEOUT_MS);

    child.on("error", () => {
      if (done) return;
      done = true;
      clearTimeout(timer);
      resolve(1);
    });

    child.on("close", (code) => {
      if (done) return;
      done = true;
      clearTimeout(timer);
      resolve(typeof code === "number" ? code : 1);
    });
  });
}

async function processUnsubscribe(rawEmail) {
  const email = normalizeEmail(rawEmail);
  if (!email) return 0;

  const exists = await emailExists(email);
  if (!exists) return 0;

  const rc = await runPhpUnsub(email);
  return rc === 0 ? 1 : 0;
}

if (require.main === module) {
  const arg = process.argv[2] || "";
  processUnsubscribe(arg)
    .then((v) => process.exit(v === 1 ? 0 : 1))
    .catch(() => process.exit(1));
}