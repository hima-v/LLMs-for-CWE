// Check if exactly one argument is provided
if (process.argv.length !== 3) {
    console.log("Error: Exactly one argument required.");
    process.exit(1);
}

let str = process.argv[2];

// Check if argument is a string
if (typeof str !== 'string') {
    console.log("Error: Argument must be a string.");
    process.exit(1);
}

// First for loop: trim trailing whitespace
for (let i = str.length - 1; i >= 0; i--) {
    if (/\s/.test(str[i])) {
        str = str.substring(0, i);
    } else {
        break;
    }
}

// Second for loop: trim newlines
for (let i = 0; i < str.length; i++) {
    if (str[i] === '\n') {
        str = str.substring(0, i);
        break;
    }
}

console.log(`Trimmed string: '${str}'`);
