// JavaScript (Node.js + Express)
// NOTE: In real systems, use POST (not URL/query params) + HTTPS to avoid credential leakage via logs, caches, referrers.

const express = require("express");
const crypto = require("crypto");

const app = express();

const EXPECTED_USERNAME = "admin";
const SALT = Buffer.from("static-demo-salt-change-me");
const ITERATIONS = 200000;
const DKLEN = 32;
const DIGEST = "sha256";

// Demo expected password = "password123"
const EXPECTED_PW_DK = crypto.pbkdf2Sync("password123", SALT, ITERATIONS, DKLEN, DIGEST);

// Simple in-memory rate limiter: 5 requests per 60 seconds per IP
const WINDOW_MS = 60_000;
const MAX_REQ = 5;
const hits = new Map(); // ip -> array of timestamps (ms)

function rateLimited(ip) {
  const now = Date.now();
  const arr = hits.get(ip) || [];
  while (arr.length && now - arr[0] > WINDOW_MS) arr.shift();
  if (arr.length >= MAX_REQ) {
    hits.set(ip, arr);
    return true;
  }
  arr.push(now);
  hits.set(ip, arr);
  return false;
}

function safeEqualString(a, b) {
  // timingSafeEqual requires same-length buffers; normalize to fixed length
  const A = Buffer.from(String(a), "utf8");
  const B = Buffer.from(String(b), "utf8");
  const len = Math.max(A.length, B.length, 1);
  const aP = Buffer.alloc(len);
  const bP = Buffer.alloc(len);
  A.copy(aP);
  B.copy(bP);
  return crypto.timingSafeEqual(aP, bP) && A.length === B.length;
}

app.get("/check_mod", (req, res) => {
  const ip = (req.headers["x-forwarded-for"] || req.socket.remoteAddress || "unknown").toString().split(",")[0].trim();

  if (rateLimited(ip)) {
    res.status(429).type("text").send("false");
    return;
  }

  const username = (req.query.username ?? "").toString();
  const password = (req.query.password ?? "").toString();

  // Constant-time comparisons (avoid leaking which field was wrong)
  const userOk = safeEqualString(username, EXPECTED_USERNAME);

  const pwDk = crypto.pbkdf2Sync(password, SALT, ITERATIONS, DKLEN, DIGEST);
  const pwOk = crypto.timingSafeEqual(pwDk, EXPECTED_PW_DK);

  const ok = userOk && pwOk;
  res.status(ok ? 200 : 401).type("text").send(ok ? "true" : "false");
});

app.listen(3000, "127.0.0.1");