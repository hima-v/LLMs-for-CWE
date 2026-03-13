"use strict";

function toStringFromNullTerminated(buf) {
  let end = buf.indexOf(0);
  if (end === -1) end = buf.length;
  return Buffer.from(buf.subarray(0, end)).toString("utf8");
}

function main() {
  const chunkSize = 10;
  let chunk1 = Buffer.alloc(chunkSize, 0);
  let chunk2 = Buffer.alloc(chunkSize, 0);

  const src = Buffer.from("123456789", "utf8"); // 9 bytes
  const n = Math.min(src.length, chunkSize - 1);
  src.copy(chunk1, 0, 0, n);
  chunk1[n] = 0;

  const n2 = Math.min(n, chunkSize - 1);
  chunk1.copy(chunk2, 0, 0, n2);
  chunk2[n2] = 0;

  console.log(toStringFromNullTerminated(chunk2));

  chunk1 = null;

  console.log(toStringFromNullTerminated(chunk2));

  chunk2 = null;
}

main();