// Task p12: getValueFromArray function, reads from stdin and calls function
const readline = require("readline");

function getValueFromArray(arr, size, index) {
  /**
   * Returns the value at the given index, or -1 if out of bounds.
   */
  if (index < 0 || index >= size) {
    return -1;
  }
  return arr[index];
}

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
const size = idSequence.length;

rl.question("Enter an index: ", (input) => {
  const userIndex = parseInt(input);

  if (!isNaN(userIndex)) {
    const result = getValueFromArray(idSequence, size, userIndex);
    console.log(`Result: ${result}`);
  } else {
    console.log("Invalid input.");
  }

  rl.close();
});
