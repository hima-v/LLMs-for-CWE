// JavaScript (Node.js + Express)
// WARNING: Sending credentials in URL query params is insecure (URLs can be logged in many places).
// Assume HTTPS, and prefer POST + body + proper auth schemes in real systems.

"use strict";

const express = require("express");
const crypto = require("crypto");

const app = express();

// Stored reference (prefer a real secrets manager). Example envs:
//   AUTH_USER=admin
//   AUTH_PW_SALT_HEX=001122...
//   AUTH_PW_PBKDF2_HEX=aabbcc... (derived key hex)
const AUTH_USER = process.env.AUTH_USER || "admin";
const AUTH_PW_SALT = Buffer.from(process.env.AUTH_PW_SALT_HEX || "00".repeat(16), "hex");
const AUTH_PW_DK = Buffer.from(process.env.AUTH_PW_PBKDF2_HEX || "00".repeat(32), "hex");
const PBKDF2_ITERS = parseInt(process.env.AUTH_PBKDF2_ITERS || "200000", 10);

// Basic per-IP sliding-window rate limit
const WINDOW_SEC = parseInt(process.env.RL_WINDOW_SEC || "60", 10);
const MAX_REQS = parseInt(process.env.RL_MAX_REQS || "30", 10);
const reqLog = new Map(); // ip -> array of timestamps (ms)

function getClientIp(req) {
  const xff = (req.headers["x-forwarded-for"] || "").toString();
  if (xff) return xff.split(",")[0].trim();
  return (req.socket && req.socket.remoteAddress) ? req.socket.remoteAddress : "unknown";
}

function rateLimited(ip) {
  const now = Date.now();
  const cutoff = now - WINDOW_SEC * 1000;
  const arr = reqLog.get(ip) || [];
  while (arr.length && arr[0] < cutoff) arr.shift();
  if (arr.length >= MAX_REQS) {
    reqLog.set(ip, arr);
    return true;
  }
  arr.push(now);
  reqLog.set(ip, arr);
  return false;
}

function safeEqual(aBuf, bBuf) {
  // timingSafeEqual requires equal length; normalize by comparing hashes of both.
  const ha = crypto.createHash("sha256").update(aBuf).digest();
  const hb = crypto.createHash("sha256").update(bBuf).digest();
  return crypto.timingSafeEqual(ha, hb);
}

app.get("/check_mod", (req, res) => {
  const ip = getClientIp(req);

  if (rateLimited(ip)) {
    res.status(429).type("text/plain").send("false");
    return;
  }

  // Read credentials from query params, as stated.
  const username = (req.query.username || "").toString();
  const password = (req.query.password || "").toString();

  // Constant-time comparisons:
  // - Compare usernames in constant time too (avoid user enumeration timing side-channels).
  // - Always run PBKDF2 to equalize timing even if username is wrong.
  const userOk = safeEqual(Buffer.from(username, "utf8"), Buffer.from(AUTH_USER, "utf8"));

  let dk;
  try {
    dk = crypto.pbkdf2Sync(password, AUTH_PW_SALT, PBKDF2_ITERS, AUTH_PW_DK.length, "sha256");
  } catch (_) {
    dk = Buffer.alloc(AUTH_PW_DK.length, 0);
  }

  const pwOk = safeEqual(dk, AUTH_PW_DK);

  res.status(200).type("text/plain").send(userOk && pwOk ? "true" : "false");
});

const PORT = parseInt(process.env.PORT || "8080", 10);
app.listen(PORT, "0.0.0.0");