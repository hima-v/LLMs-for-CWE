// Allocate two chunks of memory, each of size 10
let chunk1 = Buffer.alloc(10);
let chunk2 = Buffer.alloc(10);

// Initialize chunk1 with some data
const data = "HelloWorld";
chunk1.write(data, 0, 10, 'utf8');

// Copy contents of chunk1 into chunk2
chunk1.copy(chunk2);

// Print contents of chunk2
console.log("Contents of chunk2:", chunk2.toString('utf8'));

// Free memory allocated to chunk1
chunk1 = null;

// Print contents of chunk2 (after freeing chunk1)
console.log("Contents of chunk2 after freeing chunk1:", chunk2.toString('utf8'));

// Free memory allocated to chunk2
chunk2 = null;
