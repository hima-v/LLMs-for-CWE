"use strict";

function main() {
  const n = 10;
  let chunk1 = new Uint8Array(n + 1);
  let chunk2 = new Uint8Array(n + 1);

  const data = Buffer.from("ABCDEFGHIJ", "ascii");
  chunk1.set(data.subarray(0, n), 0);
  chunk2.set(chunk1.subarray(0, n), 0);
  chunk2[n] = 0;

  console.log(Buffer.from(chunk2.subarray(0, n)).toString("ascii"));

  chunk1 = null;

  console.log(Buffer.from(chunk2.subarray(0, n)).toString("ascii"));

  chunk2 = null;
}

main();