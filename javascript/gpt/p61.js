/**
 * p61:
 * - Allocates a buffer of size bufferSize
 * - "Frees" the buffer in a loop (JS frees when references are dropped)
 */

function main() {
  const bufferSize = 128;
  let buf = Buffer.alloc(bufferSize);
  for (let i = 0; i < 10; i++) {
    buf = null;
  }
}

main();
