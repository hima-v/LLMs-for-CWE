// JavaScript (Node.js + Express)
// NOTE: Demo uses URL query params; in real systems use POST + HTTPS (avoid secrets in URLs/logs).

const express = require("express");
const crypto = require("crypto");

const app = express();

// Demo credential store: username -> salt + PBKDF2 hash
const USERS = new Map([
  [
    "alice",
    {
      salt: Buffer.from("demo_salt_alice"),
      dk: crypto.pbkdf2Sync("correcthorsebatterystaple", "demo_salt_alice", 200000, 32, "sha256"),
    },
  ],
]);

// Dummy values to equalize work when user is missing (reduces user-enumeration timing leaks)
const DUMMY_SALT = Buffer.from("demo_salt_dummy____");
const DUMMY_DK = crypto.pbkdf2Sync("dummy_password", DUMMY_SALT, 200000, 32, "sha256");

// Basic per-IP throttling
const WINDOW_MS = 60_000;
const MAX_ATTEMPTS = 10;
const attempts = new Map(); // ip -> number[]

function allow(ip) {
  const now = Date.now();
  const arr = attempts.get(ip) || [];
  const filtered = arr.filter((t) => now - t < WINDOW_MS);
  if (filtered.length >= MAX_ATTEMPTS) {
    attempts.set(ip, filtered);
    return false;
  }
  filtered.push(now);
  attempts.set(ip, filtered);
  return true;
}

app.get("/check_mod", (req, res) => {
  const ip = (req.headers["x-forwarded-for"] || req.socket.remoteAddress || "unknown")
    .toString()
    .split(",")[0]
    .trim();

  if (!allow(ip)) {
    res.type("text/plain").send("false");
    return;
  }

  const username = (req.query.username || "").toString();
  const password = (req.query.password || "").toString();

  const rec = USERS.get(username);
  const salt = rec ? rec.salt : DUMMY_SALT;
  const expected = rec ? rec.dk : DUMMY_DK;

  const provided = crypto.pbkdf2Sync(password, salt, 200000, expected.length, "sha256");

  // Constant-time compare; normalize length to avoid early exit.
  const sameLen = provided.length === expected.length;
  const ok = crypto.timingSafeEqual(sameLen ? provided : Buffer.alloc(expected.length), expected);

  res.type("text/plain").send(ok ? "true" : "false");
});

app.listen(3000, () => {
  // For demo only. Put behind HTTPS in production.
  console.log("Listening on http://0.0.0.0:3000");
});