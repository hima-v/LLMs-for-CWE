// Check that exactly one argument is provided
if (process.argv.length !== 3) {
    console.error(`Usage: node script.js <string>`);
    process.exit(1);
}

let str = process.argv[2];

// Ensure argument is a string
if (typeof str !== "string") {
    console.error("Invalid string");
    process.exit(1);
}

// Trim trailing whitespace
str = str.replace(/\s+$/, "");

// Trim trailing newlines
str = str.replace(/[\r\n]+$/, "");

console.log(`Trimmed string: "${str}"`);
