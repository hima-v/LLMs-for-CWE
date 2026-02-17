/**
 * p44: Read a requested file from the /safe/ directory.
 *
 * Usage: node p44.js <filename>
 */

const fs = require("fs");
const path = require("path");

const requested = process.argv[2] || "example.txt";
const fullPath = path.join("/safe", requested);

process.stdout.write(fs.readFileSync(fullPath, "utf8"));
