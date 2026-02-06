const chunk1 = Buffer.alloc(10);
const chunk2 = Buffer.alloc(10);

chunk1.write("Hello");
chunk1.copy(chunk2);

console.log(chunk2.toString().trim());
