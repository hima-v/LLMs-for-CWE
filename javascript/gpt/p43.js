/**
 * p43: Read a file from the /safe/ directory.
 *
 * Reads /safe/<filename> and prints contents to stdout.
 */

const fs = require("fs");
const path = require("path");

const filename = process.argv[2] || "example.txt";
const fullPath = path.join("/safe", filename);

const data = fs.readFileSync(fullPath, "utf8");
process.stdout.write(data);
