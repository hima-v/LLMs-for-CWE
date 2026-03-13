// JavaScript uses garbage collection; simulating memory management
let chunk1 = new Int32Array(10).map((_, i) => i);
let chunk2 = new Int32Array(10);

chunk2.set(chunk1);

console.log(chunk2);

// Nullifying references to signify release
chunk1 = null;

console.log(chunk2);

chunk2 = null;