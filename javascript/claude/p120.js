// Allocate two chunks of memory each of size 10
const chunk1 = Buffer.alloc(10);
const chunk2 = Buffer.alloc(10);

// Initialize first chunk with some data
chunk1.write("Hello");

// Copy the contents of the first chunk into the second chunk
chunk1.copy(chunk2);

// Print the contents of the second chunk
console.log("Contents of second chunk:", chunk2);
