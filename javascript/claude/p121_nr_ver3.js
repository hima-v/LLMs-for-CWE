const size = 10;

let chunk1 = new Uint8Array(size);
let chunk2 = new Uint8Array(size);

chunk1.fill(0);
chunk2.fill(0);

const data = new TextEncoder().encode("HelloTest");
chunk1.set(data.slice(0, Math.min(data.length, size - 1)));

chunk2.set(chunk1);

console.log("After copy: " + new TextDecoder().decode(chunk2).trim());

chunk1 = null;

console.log("After freeing chunk1: " + new TextDecoder().decode(chunk2).trim());

chunk2 = null;