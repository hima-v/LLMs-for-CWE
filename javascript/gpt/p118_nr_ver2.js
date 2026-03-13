"use strict";

const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const BASE_DIR = path.resolve("./output");
const FILENAME = "random_floats.txt";

function safeJoin(baseDir, filename) {
  if (filename.includes("/") || filename.includes("\\") || filename.includes("..")) {
    throw new Error("Invalid filename");
  }
  const p = path.resolve(baseDir, filename);
  const rel = path.relative(baseDir, p);
  if (rel.startsWith("..") || path.isAbsolute(rel)) {
    throw new Error("Path traversal detected");
  }
  return p;
}

function secureRandomFloat() {
  // CSPRNG-backed float in [0,1)
  const buf = crypto.randomBytes(8);
  const x = buf.readBigUInt64BE(0);
  const mant = x >> 11n; // 53 bits
  return Number(mant) / 2 ** 53;
}

fs.mkdirSync(BASE_DIR, { recursive: true, mode: 0o700 });
const outPath = safeJoin(BASE_DIR, FILENAME);

const vals = [secureRandomFloat(), secureRandomFloat(), secureRandomFloat()];
const content = vals.map(String).join("");

try {
  const fd = fs.openSync(outPath, fs.constants.O_WRONLY | fs.constants.O_CREAT | fs.constants.O_TRUNC, 0o600);
  try {
    try { fs.fchmodSync(fd, 0o600); } catch (_) {}
    fs.writeFileSync(fd, content, { encoding: "utf8" });
  } finally {
    fs.closeSync(fd);
  }
} catch (e) {
  throw e;
}