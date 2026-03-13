// javascript (Node.js + Express) - secure unsubscribe via HMAC-signed token (GET)
const express = require("express");
const crypto = require("crypto");
const sqlite3 = require("sqlite3").verbose();

const app = express();
const DB_PATH = "app.db";
const UNSUB_SECRET = Buffer.from("CHANGE_ME_TO_A_LONG_RANDOM_SECRET");
const GENERIC_MSG = { message: "If the address is subscribed, it has been unsubscribed." };

const EMAIL_RE = /^[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,}$/i;

function normalizeEmail(email) {
  email = String(email || "").trim();
  if (!email || email.length > 254) throw new Error("invalid");
  email = email.toLowerCase();
  if (!EMAIL_RE.test(email)) throw new Error("invalid");
  return email;
}

function b64urlEncode(buf) {
  return buf.toString("base64").replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function b64urlDecode(s) {
  s = String(s || "").replace(/-/g, "+").replace(/_/g, "/");
  while (s.length % 4) s += "=";
  return Buffer.from(s, "base64");
}

function sign(payloadBuf) {
  return b64urlEncode(crypto.createHmac("sha256", UNSUB_SECRET).update(payloadBuf).digest());
}

function safeEqual(a, b) {
  const ab = Buffer.from(String(a));
  const bb = Buffer.from(String(b));
  if (ab.length !== bb.length) return false;
  return crypto.timingSafeEqual(ab, bb);
}

function verifyToken(token, maxAgeSeconds = 7 * 24 * 3600) {
  const raw = b64urlDecode(token).toString("utf8");
  const obj = JSON.parse(raw);
  const email = normalizeEmail(obj.email);
  const exp = Number(obj.exp);
  const sig = String(obj.sig || "");
  const now = Math.floor(Date.now() / 1000);
  if (!Number.isFinite(exp) || exp < now || exp > now + maxAgeSeconds) throw new Error("expired");

  const unsigned = Buffer.from(JSON.stringify({ email, exp }, Object.keys({ email, exp }).sort()), "utf8");
  const expected = sign(unsigned);
  if (!safeEqual(expected, sig)) throw new Error("bad sig");
  return { email, exp };
}

function withDb(fn) {
  const db = new sqlite3.Database(DB_PATH);
  db.serialize(() => fn(db));
  db.close();
}

async function mailingListUnsubscribe(email) {
  // Placeholder for ESP integration; keep idempotent.
  return;
}

app.get("/unsubscribe", async (req, res) => {
  let email;
  try {
    const data = verifyToken(req.query.t);
    email = data.email;
  } catch {
    return res.status(200).json(GENERIC_MSG);
  }

  try {
    let existed = false;
    await new Promise((resolve, reject) => {
      withDb((db) => {
        db.get("SELECT 1 FROM users WHERE email = ?", [email], (err, row) => {
          if (err) return reject(err);
          existed = !!row;
          if (!existed) return resolve();
          db.run("DELETE FROM users WHERE email = ?", [email], (err2) => {
            if (err2) return reject(err2);
            resolve();
          });
        });
      });
    });

    if (existed) await mailingListUnsubscribe(email);
  } catch {
    return res.status(200).json(GENERIC_MSG);
  }

  return res.status(200).json(GENERIC_MSG);
});

// helper: create a token (server-side usage)
function makeUnsubToken(email, ttlSeconds = 7 * 24 * 3600) {
  email = normalizeEmail(email);
  const exp = Math.floor(Date.now() / 1000) + ttlSeconds;
  const unsigned = Buffer.from(JSON.stringify({ email, exp }, Object.keys({ email, exp }).sort()), "utf8");
  const sig = sign(unsigned);
  const tokenObj = { email, exp, sig };
  return b64urlEncode(Buffer.from(JSON.stringify(tokenObj), "utf8"));
}

app.listen(3000);