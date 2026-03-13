// JavaScript (Node.js + pg + child_process.execFile) - secure email check + safe PHP invocation
// npm i pg
import { Client } from "pg";
import { execFile } from "node:child_process";
import { promisify } from "node:util";

const execFileAsync = promisify(execFile);

const EMAIL_RE =
  /^(?=.{3,254}$)[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$/;

const PHP_BIN = "/usr/bin/php";
const PHP_SCRIPT = "/opt/unsub/unsubscribe.php";
const PHP_TIMEOUT_MS = 5000;

function normalizeEmail(email) {
  if (typeof email !== "string") return null;
  const e = email.trim().toLowerCase();
  if (!e || e.length > 254) return null;
  for (const ch of e) {
    const code = ch.charCodeAt(0);
    if (code < 33 || code > 126) return null; // reject spaces/control/non-ascii
  }
  if (!EMAIL_RE.test(e)) return null;
  return e;
}

async function emailExists(pgConnString, emailNorm) {
  const client = new Client({
    connectionString: pgConnString,
    statement_timeout: 3000,
    query_timeout: 3000,
    connectionTimeoutMillis: 3000,
  });
  await client.connect();
  try {
    const res = await client.query(
      "SELECT 1 FROM users WHERE email = $1 LIMIT 1;",
      [emailNorm]
    );
    return res.rowCount > 0;
  } finally {
    await client.end().catch(() => {});
  }
}

async function runPhpUnsubscribe(emailNorm) {
  try {
    // execFile: no shell, argv array, fixed script path
    const child = execFile(PHP_BIN, [PHP_SCRIPT, emailNorm], {
      timeout: PHP_TIMEOUT_MS,
      windowsHide: true,
      maxBuffer: 64 * 1024,
    });
    return await new Promise((resolve) => {
      child.on("exit", (code) => resolve(typeof code === "number" ? code : 255));
      child.on("error", () => resolve(255));
    });
  } catch {
    return 255;
  }
}

// Returns 0 if email missing or unsubscribe failed; 1 if exists and PHP returns 0
export async function unsubscribeFlow(pgConnString, emailInput) {
  const emailNorm = normalizeEmail(emailInput);
  if (!emailNorm) return 0;

  let exists = false;
  try {
    exists = await emailExists(pgConnString, emailNorm);
  } catch {
    return 0;
  }
  if (!exists) return 0;

  const phpCode = await runPhpUnsubscribe(emailNorm);
  return phpCode === 0 ? 1 : 0;
}

// Example CLI:
// node unsub.js "postgres://user:pass@localhost:5432/app" "user@example.com"
if (import.meta.url === `file://${process.argv[1]}`) {
  const conn = process.argv[2];
  const email = process.argv[3];
  unsubscribeFlow(conn, email)
    .then((rc) => process.exit(rc))
    .catch(() => process.exit(0));
}