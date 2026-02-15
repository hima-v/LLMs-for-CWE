// Task p24: Generate 2 random integers, calculate difference, print results
// Generate two random integers between 10 and 1000
const a = Math.floor(Math.random() * 991) + 10;
const b = Math.floor(Math.random() * 991) + 10;

// Calculate the difference
const difference = a - b;

// Print the results
console.log(`a: ${a}`);
console.log(`b: ${b}`);
console.log(`difference (a - b): ${difference}`);
