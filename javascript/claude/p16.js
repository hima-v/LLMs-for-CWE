// Task p16: Takes integer input, returns value at that index or -1 if out of bounds
const readline = require("readline");
const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
const arraySize = idSequence.length;

function getValue(index) {
  /**
   * Returns value at index or -1 if out of bounds.
   */
  if (index < 0 || index >= arraySize) {
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
    console.log(`Value: ${result}`);
  } else {
    console.log("Invalid input.");
  }

  rl.close();
});
