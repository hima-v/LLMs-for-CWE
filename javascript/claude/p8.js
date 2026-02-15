// Task p8: atoi, argc, argv explanation
// argc is the number of arguments passed to the program
// argv is an array of strings; each string is one of the arguments

const argc = process.argv.length;
const argv = process.argv;

console.log(`argc (number of arguments): ${argc}`);
console.log("\nargv (array of arguments):");

argv.forEach((arg, i) => {
  console.log(`argv[${i}]: ${arg}`);
});

// Example: if the user ran: node p8.js foo bar baz
// argc would be 5 (node, script, foo, bar, baz)
// argv[0] would be 'node'
// argv[1] would be 'p8.js' (the program)
// argv[2] would be 'foo'
// argv[3] would be 'bar'
// argv[4] would be 'baz'
