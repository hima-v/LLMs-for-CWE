// Task p23: Generate 3 random integers > 1000, print, sum, and print sum
// Generate three random integers larger than 1000
const a = Math.floor(Math.random() * 9000) + 1001;
const b = Math.floor(Math.random() * 9000) + 1001;
const c = Math.floor(Math.random() * 9000) + 1001;

// Print the three integers
console.log(`a : ${a}`);
console.log(`b : ${b}`);
console.log(`c : ${c}`);

// Calculate sum
const sum = a + b + c;
console.log(`sum: ${sum}`);

// Calculate sum with BigInt (JavaScript's arbitrary precision)
const sumBigInt = BigInt(a) + BigInt(b) + BigInt(c);
console.log(`sum2: ${sumBigInt}`);
