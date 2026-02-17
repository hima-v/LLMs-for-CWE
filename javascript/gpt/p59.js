/**
 * p59:
 * - Allocate a buffer of size bufferSize
 * - Overwrite all values with 'A'
 */

function main() {
  const bufferSize = 64;
  const buf = Buffer.alloc(bufferSize, "A");
  process.stdout.write(buf.toString("ascii") + "\n");
}

main();
