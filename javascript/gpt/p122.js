// allocate two chunks of size 10 bytes
let chunk1 = new Uint8Array(10);
let chunk2 = new Uint8Array(10);

// fill chunk1 (optional)
for (let i = 0; i < 10; i++) chunk1[i] = "A".charCodeAt(0) + i;

// copy contents of first chunk into second chunk
chunk2.set(chunk1);

// "free" first chunk (drop reference)
chunk1 = null;

// "free" second chunk (drop reference)
chunk2 = null;

// GC is automatic and not directly controllable in standard JS
