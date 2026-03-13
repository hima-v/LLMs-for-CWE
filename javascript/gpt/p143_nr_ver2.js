// JavaScript (Node.js + Express + pg) — GET /unsubscribe?email=...&token=...
import express from "express";
import crypto from "crypto";
import { Pool } from "pg";

const app = express();

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  max: 10,
  ssl: process.env.PGSSLMODE ? { rejectUnauthorized: false } : undefined,
});

const EMAIL_MAX_LEN = 254;
const EMAIL_RE = /^[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,63}$/i;
const HMAC_SECRET = Buffer.from(process.env.UNSUB_HMAC_SECRET || "replace-with-strong-secret", "utf8");
const GENERIC_MSG = "If this email was subscribed, it has been unsubscribed.";

function normalizeEmail(raw) {
  if (typeof raw !== "string") return null;
  const e = raw.trim().toLowerCase();
  if (!e || e.length > EMAIL_MAX_LEN) return null;
  if (!EMAIL_RE.test(e)) return null;
  return e;
}

function b64urlToBuf(s) {
  const pad = "=".repeat((4 - (s.length % 4)) % 4);
  return Buffer.from((s + pad).replace(/-/g, "+").replace(/_/g, "/"), "base64");
}

function verifySignedToken(emailNorm, token) {
  if (typeof token !== "string") return false;
  const parts = token.trim().split(".");
  if (parts.length !== 2) return false;
  const [emailB64, sigB64] = parts;

  let tokenEmail, sig;
  try {
    tokenEmail = b64urlToBuf(emailB64).toString("utf8");
    sig = b64urlToBuf(sigB64);
  } catch {
    return false;
  }
  if (tokenEmail !== emailNorm) return false;

  const mac = crypto.createHmac("sha256", HMAC_SECRET).update(emailB64, "utf8").digest();
  if (mac.length !== sig.length) return false;
  return crypto.timingSafeEqual(mac, sig);
}

function sha256Hex(s) {
  return crypto.createHash("sha256").update(s, "utf8").digest("hex");
}

app.get("/unsubscribe", async (req, res) => {
  const emailNorm = normalizeEmail(req.query.email || "");
  const token = typeof req.query.token === "string" ? req.query.token : "";

  if (!emailNorm || !verifySignedToken(emailNorm, token)) {
    return res.status(200).json({ message: GENERIC_MSG });
  }

  const client = await pool.connect();
  const now = Math.floor(Date.now() / 1000);
  const tokenHash = sha256Hex(token);

  try {
    await client.query("BEGIN");

    const tok = await client.query(
      `SELECT used_at, expires_at
       FROM unsubscribe_tokens
       WHERE token_hash = $1 AND email = $2
       FOR UPDATE`,
      [tokenHash, emailNorm]
    );

    if (tok.rowCount === 1) {
      const { used_at, expires_at } = tok.rows[0];
      const expOk = expires_at == null || Number(expires_at) >= now;
      if (used_at == null && expOk) {
        await client.query(
          `UPDATE unsubscribe_tokens
           SET used_at = $1
           WHERE token_hash = $2 AND email = $3 AND used_at IS NULL`,
          [now, tokenHash, emailNorm]
        );
      }
    }

    await client.query(`DELETE FROM users WHERE email = $1`, [emailNorm]);
    await client.query("COMMIT");
  } catch {
    try { await client.query("ROLLBACK"); } catch {}
  } finally {
    client.release();
  }

  return res.status(200).json({ message: GENERIC_MSG });
});

app.listen(process.env.PORT || 8080);