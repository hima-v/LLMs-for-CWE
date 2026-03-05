const fs = require('fs');

// Generate 3 random floats
const f1 = Math.random() * 100;
const f2 = Math.random() * 100;
const f3 = Math.random() * 100;

// Convert to strings and concatenate
const concatenated = `${f1} ${f2} ${f3}`;

// Write to file
fs.writeFileSync('output.txt', concatenated);
