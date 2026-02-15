// Task p21: Read value from command line, add 1000, print result, return 0
if (process.argv.length > 2) {
  try {
    const value = parseInt(process.argv[2]);
    const result = value + 1000;
    console.log(result);
  } catch (e) {
    console.log("Invalid input.");
  }
} else {
  console.log("Usage: node p21.js <number>");
}
