"use strict";

const crypto = require("crypto");
const http = require("http");
const sqlite3 = require("sqlite3").verbose();

const DATABASE_PATH = "users.db";
const SUCCESS_PATH = "/success";
const CANT_LOGIN_PATH = "/cant_login";
const INTERNAL_SERVER_ERROR_PATH = "/internal_server_error";
const PBKDF2_ITERATIONS = 200000;
const KEY_LENGTH = 32;

function hasValidInput(value) {
  return typeof value === "string" && value.trim().length >= 1 && value.trim().length <= 128;
}

function verifyPassword(password, storedSalt, storedHash) {
  const computedHash = crypto.pbkdf2Sync(password, storedSalt, PBKDF2_ITERATIONS, KEY_LENGTH, "sha256").toString("hex");
  return crypto.timingSafeEqual(Buffer.from(computedHash, "utf8"), Buffer.from(storedHash, "utf8"));
}

function fetchUserRecord(username) {
  return new Promise((resolve, reject) => {
    const database = new sqlite3.Database(DATABASE_PATH, sqlite3.OPEN_READONLY, (error) => {
      if (error) {
        reject(error);
      }
    });

    database.get(
      "SELECT password_salt, password_hash FROM users WHERE username = ?",
      [username],
      (error, row) => {
        database.close();
        if (error) {
          reject(error);
          return;
        }
        resolve(row || null);
      }
    );
  });
}

async function authenticateUser(username, password) {
  const userRecord = await fetchUserRecord(username);
  if (!userRecord) {
    return false;
  }

  return verifyPassword(password, userRecord.password_salt, userRecord.password_hash);
}

const server = http.createServer((request, response) => {
  if (request.method !== "POST") {
    response.writeHead(405, { "Content-Type": "text/plain; charset=utf-8" });
    response.end("Method Not Allowed");
    return;
  }

  let body = "";
  request.setEncoding("utf8");
  request.on("data", (chunk) => {
    body += chunk;
  });

  request.on("end", async () => {
    let redirectTarget = CANT_LOGIN_PATH;

    try {
      const formData = new URLSearchParams(body);
      const username = formData.get("username");
      const password = formData.get("password");

      if (hasValidInput(username) && hasValidInput(password)) {
        redirectTarget = (await authenticateUser(username.trim(), password.trim())) ? SUCCESS_PATH : CANT_LOGIN_PATH;
      }
    } catch {
      redirectTarget = INTERNAL_SERVER_ERROR_PATH;
    }

    response.writeHead(302, { Location: redirectTarget, "Cache-Control": "no-store" });
    response.end();
  });
});

module.exports = { authenticateUser, fetchUserRecord, hasValidInput, server, verifyPassword };