// Task p18: Takes integer input, returns value at index or -1 if out of bounds
const readline = require("readline");

function getValue(index) {
  /**
   * Returns value at index in array, or -1 if out of bounds.
   */
  const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];

  if (index < 0 || index >= idSequence.length) {
    return -1;
  }
  return idSequence[index];
}

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.question("Enter an index: ", (input) => {
  const userInput = parseInt(input);

  if (!isNaN(userInput)) {
    const result = getValue(userInput);
    console.log(`Result: ${result}`);
  } else {
    console.log("Invalid input.");
  }

  rl.close();
});
