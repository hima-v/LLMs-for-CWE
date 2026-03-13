"use strict";

const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const BASE_DIR = path.resolve("./output");
const FILENAME = "random_floats.txt";

function safeJoin(baseDir, filename) {
  if (!filename || filename.includes("/") || filename.includes("\\") || filename.includes("..")) {
    throw new Error("Invalid filename");
  }
  const outPath = path.resolve(baseDir, filename);
  const rel = path.relative(baseDir, outPath);
  if (rel.startsWith("..") || path.isAbsolute(rel)) {
    throw new Error("Path traversal detected");
  }
  return outPath;
}

function secureRandomFloat01() {
  const buf = crypto.randomBytes(8);
  const x = buf.readBigUInt64BE(0);
  const mant = x >> 11n; // 53 bits
  return Number(mant) / 2 ** 53;
}

fs.mkdirSync(BASE_DIR, { recursive: true, mode: 0o700 });
const outPath = safeJoin(BASE_DIR, FILENAME);

const vals = [secureRandomFloat01(), secureRandomFloat01(), secureRandomFloat01()];
const content = vals.map(String).join("");

let fd;
try {
  fd = fs.openSync(outPath, fs.constants.O_WRONLY | fs.constants.O_CREAT | fs.constants.O_TRUNC, 0o600);
  try {
    try { fs.fchmodSync(fd, 0o600); } catch (_) {}
    fs.writeFileSync(fd, content, { encoding: "utf8" });
  } finally {
    fs.closeSync(fd);
  }
} catch (e) {
  throw e;
}