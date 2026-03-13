// javascript (Node.js + Express)
// NOTE: This is a constrained demo using URL query params. In real systems, use POST + HTTPS (and avoid credentials in URLs/logs).

const express = require("express");
const crypto = require("crypto");

const app = express();

// ----- Simple in-memory rate limiting (per IP) -----
const WINDOW_MS = 60_000;
const MAX_ATTEMPTS = 10;
const attempts = new Map(); // ip -> timestamps[]

function isRateLimited(ip) {
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

// ----- Password hashing (scrypt) + constant-time checks -----
function scryptKey(password, salt) {
  return crypto.scryptSync(password, salt, 32, { N: 1 << 15, r: 8, p: 1 });
}

// Demo "user database": username -> { salt: Buffer, dk: Buffer }
const USER_DB = new Map();
function addUser(username, password) {
  const salt = crypto.randomBytes(16);
  const dk = scryptKey(password, salt);
  USER_DB.set(username, { salt, dk });
}
addUser("alice", "CorrectHorseBatteryStaple!");

const DUMMY_SALT = Buffer.alloc(16, 0);
const DUMMY_DK = scryptKey("dummy-password", DUMMY_SALT);

app.get("/check_mod", (req, res) => {
  const xf = (req.headers["x-forwarded-for"] || "").toString();
  const ip = (xf.split(",")[0].trim() || req.socket.remoteAddress || "unknown");

  if (isRateLimited(ip)) {
    setTimeout(() => {
      res.type("text/plain").send("false");
    }, 250);
    return;
  }

  const username = (req.query.username || "").toString();
  const password = (req.query.password || "").toString();

  const record = USER_DB.get(username);
  const salt = record ? record.salt : DUMMY_SALT;
  const expectedDk = record ? record.dk : DUMMY_DK;

  const providedDk = scryptKey(password, salt);

  // Constant-time compare; also ensure username exists without revealing why it failed
  const same = (providedDk.length === expectedDk.length) &&
    crypto.timingSafeEqual(providedDk, expectedDk);

  const ok = same && USER_DB.has(username);

  res.type("text/plain").send(ok ? "true" : "false");
});

app.listen(3000, "127.0.0.1");