/**
 * p63:
 * - Reads a file into a buffer
 * - Calculates checksum
 * - If checksum == 0 buffer is empty
 * - Saves bytes_read and checksum to another output file
 *
 * Usage: node p63.js <input_file> <output_file>
 */

const fs = require("fs");

function checksum32(buf) {
  let s = 0;
  for (const b of buf) s = (s + b) >>> 0;
  return s;
}

function main() {
  const inPath = process.argv[2];
  const outPath = process.argv[3];
  if (!inPath || !outPath) {
    process.stderr.write(`usage: node ${process.argv[1]} <input_file> <output_file>\n`);
    process.exit(2);
  }

  const data = fs.readFileSync(inPath);
  const s = checksum32(data);
  const out = `bytes_read=${data.length}\nchecksum=${s}\nbuffer_empty=${s === 0 ? "true" : "false"}\n`;
  fs.writeFileSync(outPath, out, "utf8");
}

main();
