// node.js
"use strict";

const path = require("path");
const { spawn } = require("child_process");
const sqlite3 = require("sqlite3").verbose();

const DB_PATH = "/var/app/data/app.db"; // fixed path
const PHP_BIN = "/usr/bin/php";         // fixed path
const PHP_SCRIPT = "/var/app/scripts/unsubscribe.php"; // fixed path
const PHP_TIMEOUT_MS = 3000;

const EMAIL_RE = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+$/;

function normalizeEmail(email) {
  if (typeof email !== "string") return "";
  return email.trim().toLowerCase();
}

function isValidEmail(email) {
  if (!email || email.length > 254) return false;
  return EMAIL_RE.test(email);
}

function emailExists(db, email) {
  return new Promise((resolve) => {
    db.get("SELECT 1 FROM subscribers WHERE email = ? LIMIT 1", [email], (err, row) => {
      if (err) return resolve(false);
      resolve(!!row);
    });
  });
}

function callPhpUnsubscribe(email) {
  return new Promise((resolve) => {
    const child = spawn(PHP_BIN, [PHP_SCRIPT, email], {
      stdio: ["ignore", "pipe", "pipe"],
      windowsHide: true,
    });

    let stdout = "";
    let stderr = "";
    let settled = false;

    const timer = setTimeout(() => {
      if (settled) return;
      settled = true;
      child.kill("SIGKILL");
      resolve(false);
    }, PHP_TIMEOUT_MS);

    child.stdout.on("data", (d) => { stdout += d.toString("utf8"); });
    child.stderr.on("data", (d) => { stderr += d.toString("utf8"); });

    child.on("error", () => {
      clearTimeout(timer);
      if (settled) return;
      settled = true;
      resolve(false);
    });

    child.on("close", (code) => {
      clearTimeout(timer);
      if (settled) return;
      settled = true;

      if (code !== 0) return resolve(false);
      const out = (stdout || "").trim();
      resolve(out === "0");
    });
  });
}

async function unsubscribeFlow(emailInput) {
  const email = normalizeEmail(emailInput);
  if (!isValidEmail(email)) return 0;

  const db = new sqlite3.Database(DB_PATH, sqlite3.OPEN_READONLY, async (err) => {});
  const closeDb = () => new Promise((r) => db.close(() => r()));

  try {
    const exists = await emailExists(db, email);
    if (!exists) return 0;

    const ok = await callPhpUnsubscribe(email);
    return ok ? 1 : 0;
  } catch {
    return 0;
  } finally {
    await closeDb();
  }
}

(async () => {
  const emailArg = process.argv[2] || "";
  const result = await unsubscribeFlow(emailArg);
  process.stdout.write(String(result) + "\n");
})();