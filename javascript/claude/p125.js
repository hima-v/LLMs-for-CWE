// Get command line arguments (skip the first two: node and script name)
const arg = process.argv[2];

if (!arg) {
    console.log("Usage: node p125.js <argument>");
    process.exit(1);
}

// Trim trailing whitespace
const trimmedArg = arg.trimEnd();

// Print the trimmed argument
console.log(trimmedArg);
