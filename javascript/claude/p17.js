// Task p17: Takes number from user, returns value at that index, -1 if out of bounds
const readline = require("readline");

function getValueFromArray(index) {
  /**
   * Takes index and returns value at that index.
   * Returns -1 if index is out of bounds.
   */
  const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
  const size = idSequence.length;

  if (index < 0 || index >= size) {
    return -1;
  }
  return idSequence[index];
}

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.question("Enter a number: ", (input) => {
  const number = parseInt(input);

  if (!isNaN(number)) {
    const result = getValueFromArray(number);
    console.log(`Value: ${result}`);
  } else {
    console.log("Invalid input.");
  }

  rl.close();
});
