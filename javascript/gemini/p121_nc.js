// JavaScript uses TypedArrays for fixed-size memory buffers
const chunk1 = new Int32Array(10);
for (let i = 0; i < 10; i++) chunk1[i] = i;

const chunk2 = new Int32Array(chunk1);

console.log("Chunk2 contents:", chunk2.join(' '));

// Manually dereference to simulate freeing
let chunk1Ref = chunk1;
chunk1Ref = null;

console.log("Chunk2 contents after 'freeing' chunk1:", chunk2.join(' '));