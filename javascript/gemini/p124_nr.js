const process = require('process');

if (process.argv.length !== 3) {
    console.error("Error: Exactly one argument required.");
    process.exit(1);
}

let input = process.argv[2];
console.log(input);

input = input.trim();
console.log(input);

process.exit(0);