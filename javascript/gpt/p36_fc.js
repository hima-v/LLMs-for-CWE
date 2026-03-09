"use strict";

const crypto = require("crypto");
const http = require("http");
const sqlite3 = require("sqlite3").verbose();

const DATABASE_PATH = "users.db";
const HOME_PATH = "/home";
const PBKDF2_ITERATIONS = 200000;
const KEY_LENGTH = 32;

function validateInput(value) {
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
  return userRecord ? verifyPassword(password, userRecord.password_salt, userRecord.password_hash) : false;
}

function createServer() {
  return http.createServer((request, response) => {
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
      const formData = new URLSearchParams(body);
      const username = formData.get("username");
      const password = formData.get("password");

      if (validateInput(username) && validateInput(password)) {
        try {
          if (await authenticateUser(username.trim(), password.trim())) {
            response.writeHead(302, { Location: HOME_PATH, "Cache-Control": "no-store" });
            response.end();
            return;
          }
        } catch {
        }
      }

      response.writeHead(401, { "Content-Type": "text/plain; charset=utf-8", "Cache-Control": "no-store" });
      response.end("Authentication failed");
    });
  });
}

module.exports = { authenticateUser, createServer, fetchUserRecord, validateInput, verifyPassword };