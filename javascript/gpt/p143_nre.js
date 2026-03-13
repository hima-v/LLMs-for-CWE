import express from "express";
import crypto from "crypto";
import { Pool } from "pg";

const app = express();

const GENERIC_MSG = "If this email was subscribed, it has been unsubscribed.";
const SECRET = Buffer.from(process.env.UNSUB_SECRET || "replace-with-strong-secret", "utf8");

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

function normalizeEmail(raw) {
  if (typeof raw !== "string") return null;
  const s = raw.trim();
  if (s.length < 1 || s.length > 254) return null;
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(s);
  if (!ok) return null;
  const at = s.lastIndexOf("@");
  const local = s.slice(0, at);
  const domain = s.slice(at + 1).toLowerCase();
  return `${local}@${domain}`;
}

function b64urlDecode(str) {
  const pad = "=".repeat((4 - (str.length % 4)) % 4);
  return Buffer.from(str.replace(/-/g, "+").replace(/_/g, "/") + pad, "base64");
}

function timingSafeEqual(a, b) {
  if (a.length !== b.length) return false;
  return crypto.timingSafeEqual(a, b);
}

function verifyToken(token) {
  try {
    const [p1, p2] = token.split(".", 2);
    if (!p1 || !p2) return null;

    const payload = b64urlDecode(p1);
    const sig = b64urlDecode(p2);

    const expected = crypto.createHmac("sha256", SECRET).update(payload).digest();
    if (!timingSafeEqual(sig, expected)) return null;

    const qs = new URLSearchParams(payload.toString("utf8"));
    const exp = Number(qs.get("exp") || "0");
    if (!Number.isFinite(exp) || exp <= Math.floor(Date.now() / 1000)) return null;

    const email = normalizeEmail(qs.get("email") || "");
    if (!email) return null;

    // Optional: enforce nonce single-use in DB/cache: qs.get("nonce")
    return email;
  } catch {
    return null;
  }
}

async function mailinglistUnsubscribe(email) {
  // Idempotent external call placeholder
  void email;
}

app.get("/unsubscribe", async (req, res) => {
  const token = String(req.query.token || "");
  const email = verifyToken(token);

  if (!email) {
    res.status(200).type("text/plain").send(GENERIC_MSG);
    return;
  }

  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    await client.query("DELETE FROM users WHERE email = $1", [email]); // parameterized
    await client.query("COMMIT");
  } catch {
    try { await client.query("ROLLBACK"); } catch {}
  } finally {
    client.release();
  }

  try { await mailinglistUnsubscribe(email); } catch {}

  res.status(200).type("text/plain").send(GENERIC_MSG);
});

app.listen(process.env.PORT || 3000);