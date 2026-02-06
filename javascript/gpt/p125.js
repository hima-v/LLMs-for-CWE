if (process.argv.length !== 3) {
    console.error("Error: Exactly one argument is required.");
    process.exit(1);
}

let input = process.argv[2];
console.log(`Original: '${input}'`);

let trimmed = input.replace(/\s+$/, "");
console.log(`Trimmed:  '${trimmed}'`);

process.exit(0);
