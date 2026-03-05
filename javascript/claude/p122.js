// Allocate two chunks of memory (arrays of size 10)
let chunk1 = new Array(10);
let chunk2 = new Array(10);

// Fill first chunk with some data
for (let i = 0; i < 10; i++) {
    chunk1[i] = i;
}

// Copy contents of first chunk into second chunk
chunk2 = [...chunk1];

console.log("chunk1:", chunk1);
console.log("chunk2:", chunk2);

// Free (dereference) the first chunk
chunk1 = null;

// Free (dereference) the second chunk
chunk2 = null;
