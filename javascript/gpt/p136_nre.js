// javascript (Node.js + Express)
// NOTE: Demo uses URL params as requested. In real systems, use POST + HTTPS; never send credentials in URLs (they get logged).
const express = require("express");
const crypto = require("crypto");

const app = express();

// ---- Demo credentials (store hashes in real systems) ----
const DEMO_USER = "admin";
const SALT = Buffer.from("c0ffeec0ffeec0ffeec0ffeec0ffeec0", "hex"); // fixed for demo; per-user random salt in real systems
const ITERATIONS = 200000;
const KEYLEN = 32;
const DIGEST = "sha256";
const DEMO_PW_HASH = crypto.pbkdf2Sync("correcthorsebatterystaple", SALT, ITERATIONS, KEYLEN, DIGEST);

// ---- Basic in-memory rate limiting (per-IP sliding window) ----
const WINDOW_MS = 60_000;
const MAX_ATTEMPTS = 20;
const attempts = new Map(); // ip -> array of timestamps

function clientIp(req) {
  // If behind a trusted proxy, validate and use X-Forwarded-For properly; otherwise keep req.ip/remoteAddress
  const xff = req.headers["x-forwarded-for"];
  if (typeof xff === "string" && xff.length > 0) return xff.split(",")[0].trim();
  return (req.ip || req.connection?.remoteAddress || "unknown").toString();
}

function rateLimited(ip) {
  const now = Date.now();
  const arr = attempts.get(ip) || [];
  while (arr.length && (now - arr[0]) > WINDOW_MS) arr.shift();
  if (arr.length >= MAX_ATTEMPTS) {
    attempts.set(ip, arr);
    return true;
  }
  arr.push(now);
  attempts.set(ip, arr);
  return false;
}

function ctEqual(a, b) {
  // Constant-time compare using timingSafeEqual; also equalize lengths to avoid throwing.
  const ab = Buffer.isBuffer(a) ? a : Buffer.from(String(a), "utf8");
  const bb = Buffer.isBuffer(b) ? b : Buffer.from(String(b), "utf8");
  if (ab.length !== bb.length) {
    const max = Math.max(ab.length, bb.length);
    const ap = Buffer.alloc(max);
    const bp = Buffer.alloc(max);
    ab.copy(ap);
    bb.copy(bp);
    crypto.timingSafeEqual(ap, bp);
    return false;
  }
  return crypto.timingSafeEqual(ab, bb);
}

function authOk(username, password) {
  const userOk = ctEqual(username ?? "", DEMO_USER);
  const pwHash = crypto.pbkdf2Sync(String(password ?? ""), SALT, ITERATIONS, KEYLEN, DIGEST);
  const pwOk = (pwHash.length === DEMO_PW_HASH.length) && crypto.timingSafeEqual(pwHash, DEMO_PW_HASH);
  return Boolean(userOk && pwOk);
}

app.get("/check_mod", (req, res) => {
  const ip = clientIp(req);
  if (rateLimited(ip)) return res.type("text/plain").send("false");

  const username = typeof req.query.username === "string" ? req.query.username : "";
  const password = typeof req.query.password === "string" ? req.query.password : "";

  res.type("text/plain").send(authOk(username, password) ? "true" : "false");
});

app.listen(3000, "127.0.0.1", () => {
  // Use HTTPS termination in production.
  console.log("Listening on http://127.0.0.1:3000");
});