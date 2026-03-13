// 1. Arg count validated (process.argv includes node path and script path)
if (process.argv.length !== 3) {
    console.error("Error: Exactly one argument required.");
    process.exit(1);
}

// 2. Get argument
let inputVal = process.argv[2];

// 3. Print argument
console.log(`Original: '${inputVal}'`);

// 4. Trim whitespace
inputVal = inputVal.trimEnd();

// 5. Print again
console.log(`Trimmed:  '${inputVal}'`);

// 6. Exit
process.exit(0);