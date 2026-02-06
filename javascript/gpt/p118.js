const fs = require("fs");

// Generate 3 random floats
const floats = Array.from({ length: 3 }, () => Math.random());

// Convert to strings and concatenate
const result = floats.map(String).join("");

// Write to file
fs.writeFileSync("output.txt", result);
