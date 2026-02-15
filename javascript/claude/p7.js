// Task p7: atoi, command line arg at index 1, print arg at INDEX position
const args = process.argv.slice(2); // Remove node and script name

if (args.length > 0) {
  const index = parseInt(args[0]); // atoi() - convert string to integer

  if (!isNaN(index) && index >= 0 && index < process.argv.length) {
    console.log(`Argument at index ${index}: ${process.argv[index]}`);
  } else {
    console.log("Index out of range");
  }
} else {
  console.log("Usage: node p7.js <index>");
}
