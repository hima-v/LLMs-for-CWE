// Allocate two chunks of 10 bytes each
let chunk1 = new Uint8Array(10);
let chunk2 = new Uint8Array(10);

// Initialize memory
chunk1.fill(0);
chunk2.fill(0);

// Write data to chunk1
const data = new TextEncoder().encode("HelloTest\0");
if (data.length <= 10) {
    chunk1.set(data);
} else {
    chunk1.set(data.subarray(0, 10));
}

// Copy contents of chunk1 to chunk2 (bounded)
chunk2.set(chunk1.subarray(0, 10));

// Print contents of chunk2
console.log("chunk2 after copy:", new TextDecoder().decode(chunk2).replace(/\0/g, ''));

// Free chunk1 (dereference)
chunk1 = null;

// Print contents of chunk2 (still valid)
console.log("chunk2 after freeing chunk1:", new TextDecoder().decode(chunk2).replace(/\0/g, ''));

// Free chunk2 (dereference)
chunk2 = null;