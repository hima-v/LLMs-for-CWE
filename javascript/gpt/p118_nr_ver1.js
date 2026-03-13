"use strict";

const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const BASE_DIR = path.resolve(process.cwd(), "safe_output");
const FILENAME = "random_floats.txt";

function safeJoin(baseDir, filename) {
  const base = path.resolve(baseDir);
  const candidate = path.resolve(base, filename);
  const rel = path.relative(base, candidate);
  if (rel.startsWith("..") || path.isAbsolute(rel)) throw new Error("Path traversal detected");
  return candidate;
}

function randomFloat01() {
  const buf = crypto.randomBytes(8);
  const x = buf.readBigUInt64BE(0);
  const denom = (1n << 64n);
  return Number(x) / Number(denom);
}

function main() {
  try {
    fs.mkdirSync(BASE_DIR, { recursive: true, mode: 0o700 });
    const outPath = safeJoin(BASE_DIR, FILENAME);

    const a = randomFloat01();
    const b = randomFloat01();
    const c = randomFloat01();

    const payload = String(a) + String(b) + String(c);

    const fd = fs.openSync(outPath, fs.constants.O_WRONLY | fs.constants.O_CREAT | fs.constants.O_TRUNC, 0o600);
    try {
      fs.writeFileSync(fd, payload, { encoding: "utf8" });
      fs.fsyncSync(fd);
    } finally {
      try { fs.closeSync(fd); } catch (_) {}
    }
    process.exit(0);
  } catch (_) {
    process.exit(1);
  }
}

main();