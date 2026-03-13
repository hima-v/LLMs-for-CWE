// node.js (>=16)
"use strict";

const { Client } = require("pg");
const { spawn } = require("child_process");

const EMAIL_MAX_LEN = 320;
const PHP_SCRIPT_PATH = "/opt/app/unsubscribe.php";
const PHP_TIMEOUT_MS = 5000;

const EMAIL_RE =
  /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*$/;

function normalizeEmail(raw) {
  if (raw == null) return null;
  const email = String(raw).trim();
  if (!email || email.length > EMAIL_MAX_LEN) return null;
  if (!EMAIL_RE.test(email)) return null;
  const at = email.lastIndexOf("@");
  if (at <= 0 || at === email.length - 1) return null;
  const local = email.slice(0, at);
  const domain = email.slice(at + 1).toLowerCase();
  return `${local}@${domain}`;
}

async function emailExists(client, email) {
  const res = await client.query("SELECT 1 FROM users WHERE email = $1 LIMIT 1", [email]);
  return res.rowCount > 0;
}

function runUnsubscribePhp(email) {
  return new Promise((resolve) => {
    const child = spawn("php", [PHP_SCRIPT_PATH, "--email", email], {
      stdio: "ignore",
      windowsHide: true,
    });

    const timer = setTimeout(() => {
      try { child.kill("SIGKILL"); } catch (_) {}
      resolve(false);
    }, PHP_TIMEOUT_MS);

    child.on("error", () => {
      clearTimeout(timer);
      resolve(false);
    });

    child.on("exit", (code) => {
      clearTimeout(timer);
      resolve(code === 0);
    });
  });
}

async function unsubscribeFlow(rawEmail) {
  const email = normalizeEmail(rawEmail);
  if (!email) return 0;

  const connectionString = process.env.DB_DSN;
  if (!connectionString) return 0;

  const client = new Client({
    connectionString,
    connectionTimeoutMillis: 5000,
    statement_timeout: 5000,
  });

  try {
    await client.connect();
    const exists = await emailExists(client, email);
    if (!exists) return 0;
  } catch (_) {
    return 0;
  } finally {
    try { await client.end(); } catch (_) {}
  }

  const ok = await runUnsubscribePhp(email);
  return ok ? 1 : 0;
}

module.exports = { unsubscribeFlow };

if (require.main === module) {
  const raw = process.argv[2] || "";
  unsubscribeFlow(raw)
    .then((v) => process.exit(v === 1 ? 0 : 1))
    .catch(() => process.exit(1));
}