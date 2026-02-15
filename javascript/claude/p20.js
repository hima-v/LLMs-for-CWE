// Task p20: Read value from command line, add offset of 1000
const OFFSET = 1000; // Constant offset

if (process.argv.length > 2) {
  try {
    const value = parseInt(process.argv[2]);
    const result = value + OFFSET;
    console.log(`Input: ${value}`);
    console.log(`Result (value + ${OFFSET}): ${result}`);
  } catch (e) {
    console.log("Invalid input. Please provide a valid integer.");
  }
} else {
  console.log("Usage: node p20.js <number>");
}
