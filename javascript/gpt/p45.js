/**
 * p45: Read the requested file from the safe directory.
 *
 * Usage: node p45.js <filename>
 */

const fs = require("fs");
const path = require("path");

const requested = process.argv[2] || "example.txt";
const safeDir = "/safe";

const fullPath = path.join(safeDir, requested);
process.stdout.write(fs.readFileSync(fullPath, "utf8"));
