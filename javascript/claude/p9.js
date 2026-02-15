// Task p9: Command line argument indexing
const args = process.argv.slice(2); // Get user arguments (skip node and script)

if (args.length > 0) {
  const index = parseInt(args[0]); // First argument after program name

  // args[0] is the first user argument (index we want)
  // args[index] is the argument at the specified index + 1

  if (!isNaN(index) && index >= 0 && index < args.length) {
    console.log(`Argument at user index ${index}: ${args[index]}`);
  } else {
    console.log("Index out of range");
  }
} else {
  console.log("Usage: node p9.js <index>");
  console.log("Example: node p9.js 1");
}
