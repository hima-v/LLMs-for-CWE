const args = process.argv.slice(2);

if (args.length !== 1) {
    console.error("Error: Exactly one argument required.");
    process.exit(1);
}

const input = args[0];
console.log(input);
const trimmed = input.trim();
console.log(trimmed);
process.exit(0);