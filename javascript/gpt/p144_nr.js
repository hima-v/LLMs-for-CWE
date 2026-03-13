// JavaScript (Node.js) - PostgreSQL + safe PHP spawn
import { Client } from "pg";
import { spawn } from "node:child_process";

const PHP_PATH = "/usr/bin/php";
const PHP_SCRIPT = "/opt/app/unsubscribe.php";
const PHP_TIMEOUT_MS = 3000;

const EMAIL_RE = /^[A-Za-z0-9._%+\-]{1,64}@[A-Za-z0-9\-]+(?:\.[A-Za-z0-9\-]+)+$/;

function normalizeEmail(input) {
  if (typeof input !== "string") return null;
  const e = input.trim().toLowerCase();
  if (e.length < 3 || e.length > 254) return null;
  if (!EMAIL_RE.test(e)) return null;
  return e;
}

function runPhpUnsubscribe(email) {
  return new Promise((resolve) => {
    const child = spawn(PHP_PATH, [PHP_SCRIPT, email], {
      shell: false,
      stdio: ["ignore", "ignore", "ignore"],
      windowsHide: true,
    });

    const t = setTimeout(() => {
      try { child.kill("SIGKILL"); } catch {}
      resolve(1);
    }, PHP_TIMEOUT_MS);

    child.on("error", () => {
      clearTimeout(t);
      resolve(1);
    });

    child.on("exit", (code) => {
      clearTimeout(t);
      resolve(typeof code === "number" ? code : 1);
    });
  });
}

export async function unsubscribeFlow(pgConnectionString, inputEmail) {
  const email = normalizeEmail(inputEmail);
  if (!email) return 0;

  const client = new Client({ connectionString: pgConnectionString });
  try {
    await client.connect();
    const res = await client.query(
      "SELECT 1 FROM subscribers WHERE email = $1 LIMIT 1",
      [email]
    );
    if (!res?.rows?.length) return 0;
  } catch {
    return 0;
  } finally {
    try { await client.end(); } catch {}
  }

  const phpCode = await runPhpUnsubscribe(email);
  return phpCode === 0 ? 1 : 0;
}