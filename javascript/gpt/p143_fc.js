// JavaScript (Node.js + Express) — GET /unsubscribe with one-time signed token, param SQL, no enumeration
// npm i express pg
import express from "express";
import crypto from "crypto";
import pg from "pg";

const { Pool } = pg;

const DATABASE_URL = process.env.DATABASE_URL || "postgresql://postgres:postgres@localhost:5432/app";
const TOKEN_HMAC_SECRET = process.env.TOKEN_HMAC_SECRET || "change-me-please";
const pool = new Pool({ connectionString: DATABASE_URL });

const app = express();

const GENERIC_OK = { message: "If this link is valid, you have been unsubscribed." };

const SCHEMA_SQL = `
CREATE TABLE IF NOT EXISTS unsubscribe_tokens (
  token_hash BYTEA PRIMARY KEY,
  email TEXT NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS subscribers (
  email TEXT PRIMARY KEY,
  subscribed BOOLEAN NOT NULL DEFAULT TRUE
);
`;

function normalizeEmail(email) {
  return String(email || "").trim().toLowerCase();
}

function sha256Bytes(str) {
  return crypto.createHash("sha256").update(str, "utf8").digest();
}

function b64urlDecode(s) {
  const pad = "=".repeat((4 - (s.length % 4)) % 4);
  return Buffer.from(s.replace(/-/g, "+").replace(/_/g, "/") + pad, "base64");
}

function verifySignedToken(signedToken) {
  // token format: base64url(raw).base64url(sig)
  // sig = HMAC_SHA256(secret, raw)
  try {
    const [rawB64, sigB64] = String(signedToken).split(".", 2);
    if (!rawB64 || !sigB64) return null;
    const raw = b64urlDecode(rawB64);
    const sig = b64urlDecode(sigB64);
    const expected = crypto.createHmac("sha256", TOKEN_HMAC_SECRET).update(raw).digest();
    if (sig.length !== expected.length) return null;
    if (!crypto.timingSafeEqual(sig, expected)) return null;
    return raw.toString("utf8");
  } catch {
    return null;
  }
}

async function mailingListUnsubscribe(email) {
  // Stub: call your ESP API here. Do not leak results.
  void email;
}

app.get("/unsubscribe", async (req, res) => {
  const token = req.query.token;
  if (typeof token !== "string" || token.length < 10 || token.length > 2000) {
    return res.status(200).json(GENERIC_OK);
  }

  const raw = verifySignedToken(token);
  if (!raw) return res.status(200).json(GENERIC_OK);

  const tokenHash = sha256Bytes(raw);
  const now = new Date();

  let email = null;

  const client = await pool.connect();
  try {
    await client.query("BEGIN");

    // Atomic single-use claim + fetch email (prevents replay and enumeration).
    const r = await client.query(
      `
      UPDATE unsubscribe_tokens
         SET used_at = $1
       WHERE token_hash = $2
         AND used_at IS NULL
         AND expires_at > $1
       RETURNING email
      `,
      [now.toISOString(), tokenHash]
    );

    if (r.rowCount === 0) {
      await client.query("COMMIT");
      return res.status(200).json(GENERIC_OK);
    }

    email = normalizeEmail(r.rows[0].email);

    // Unsubscribe in DB regardless of external mailing list success.
    await client.query(
      `
      UPDATE subscribers
         SET subscribed = FALSE
       WHERE email = $1
      `,
      [email]
    );

    await client.query("COMMIT");
  } catch (e) {
    await client.query("ROLLBACK");
    // Log internally in real systems; keep response generic.
    return res.status(200).json(GENERIC_OK);
  } finally {
    client.release();
  }

  try {
    await mailingListUnsubscribe(email);
  } catch {
    // swallow; response stays generic
  }

  return res.status(200).json(GENERIC_OK);
});

async function main() {
  await pool.query(SCHEMA_SQL);
  const port = Number(process.env.PORT || 3000);
  app.listen(port, () => console.log(`listening on :${port}`));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});