"use strict";

const crypto = require("crypto");

const LOGIN_PATH = "/login";
const INDEX_PATH = "/";
const USER_PATH = "/user";
const ADMIN_PATH = "/admin";
const PBKDF2_ITERATIONS = 200000;
const USERS = {
  user: { role: "user", salt: "user-salt", hash: crypto.pbkdf2Sync("user-password", "user-salt", PBKDF2_ITERATIONS, 32, "sha256").toString("hex") },
  admin: { role: "admin", salt: "admin-salt", hash: crypto.pbkdf2Sync("admin-password", "admin-salt", PBKDF2_ITERATIONS, 32, "sha256").toString("hex") },
};
const SESSIONS = new Map();

function hasValidInput(value) {
  return typeof value === "string" && value.trim().length >= 1 && value.trim().length <= 128;
}

function verifyPassword(username, password) {
  const user = USERS[username];
  if (!user) {
    return false;
  }
  const computedHash = crypto.pbkdf2Sync(password, user.salt, PBKDF2_ITERATIONS, 32, "sha256").toString("hex");
  return crypto.timingSafeEqual(Buffer.from(computedHash, "utf8"), Buffer.from(user.hash, "utf8"));
}

function parseCookies(request) {
  const cookieHeader = request.headers.cookie || "";
  const cookies = {};
  for (const part of cookieHeader.split(";")) {
    const [key, value] = part.trim().split("=", 2);
    if (key && value) {
      cookies[key] = value;
    }
  }
  return cookies;
}

function getSession(request) {
  const sessionId = parseCookies(request).session_id;
  return sessionId ? SESSIONS.get(sessionId) : undefined;
}

function send(response, statusCode, body, headers = {}) {
  response.writeHead(statusCode, { "Content-Type": "text/plain; charset=utf-8", "Cache-Control": "no-store", ...headers });
  response.end(body);
}

function login(_request, response) {
  send(response, 200, "Login page");
}

function doLogin(_request, response, body) {
  const form = new URLSearchParams(body);
  const username = form.get("username");
  const password = form.get("password");

  if (!hasValidInput(username) || !hasValidInput(password)) {
    send(response, 400, "Invalid credentials");
    return;
  }

  const normalizedUsername = username.trim();
  const normalizedPassword = password.trim();
  if (!verifyPassword(normalizedUsername, normalizedPassword)) {
    send(response, 401, "Authentication failed");
    return;
  }

  const sessionId = crypto.randomBytes(24).toString("base64url");
  SESSIONS.set(sessionId, { username: normalizedUsername, role: USERS[normalizedUsername].role });
  send(response, 302, "", { Location: INDEX_PATH, "Set-Cookie": `session_id=${sessionId}; HttpOnly; SameSite=Strict` });
}

function index(request, response) {
  const session = getSession(request);
  if (!session) {
    send(response, 302, "", { Location: LOGIN_PATH });
    return;
  }
  send(response, 200, `Welcome ${session.username}`);
}

function userPage(request, response) {
  const session = getSession(request);
  if (!session) {
    send(response, 302, "", { Location: LOGIN_PATH });
    return;
  }
  send(response, 200, "User page");
}

function adminPage(request, response) {
  const session = getSession(request);
  if (!session) {
    send(response, 302, "", { Location: LOGIN_PATH });
    return;
  }
  if (session.role !== "admin") {
    send(response, 403, "Forbidden");
    return;
  }
  send(response, 200, "Admin page");
}

module.exports = { adminPage, doLogin, index, login, userPage };