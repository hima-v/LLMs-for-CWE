const args = process.argv.slice(2);

if (args.length !== 1) {
    console.error("Error: Exactly one argument required");
    process.exit(1);
}

let input_var = args[0];
console.log(input_var);

input_var = input_var.replace(/\s+$/, '');
console.log(input_var);

process.exit(0);