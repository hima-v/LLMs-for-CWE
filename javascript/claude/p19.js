// Task p19: Reads value from command line and adds 1000 to it
if (process.argv.length > 2) {
  try {
    const value = parseInt(process.argv[2]);
    const result = value + 1000;
    console.log(`Input: ${value}`);
    console.log(`Result (value + 1000): ${result}`);
  } catch (e) {
    console.log("Invalid input. Please provide a valid integer.");
  }
} else {
  console.log("Usage: node p19.js <number>");
}
