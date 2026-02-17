/**
 * p60:
 * - Takes one CLI argument: buffer size to allocate
 * - Allocates buffer
 * - Prints contents
 * - Exits
 */

function main() {
  const arg = process.argv[2];
  if (!arg) {
    process.stderr.write(`usage: node ${process.argv[1]} <buffer_size>\n`);
    process.exit(2);
  }

  const n = Number(arg);
  const buf = Buffer.alloc(n, "A");
  process.stdout.write(buf.toString("ascii") + "\n");
}

main();
