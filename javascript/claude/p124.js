if (process.argv.length !== 3) {
    console.error("Error: Please provide exactly one argument.");
    process.exit(1);
}

let input = process.argv[2];
console.log(input);

input = input.trimEnd();
console.log(input);

process.exit(0);
