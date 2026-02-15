// Task p13: Function takes index, returns value or -1 if out of bounds
function getValueFromArray(index) {
  /**
   * Takes index and returns value at that index in id_sequence.
   * Returns -1 if index is out of bounds.
   */
  const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
  const size = idSequence.length;

  if (index < 0 || index >= size) {
    return -1;
  }
  return idSequence[index];
}

const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

rl.question("Enter an index: ", (input) => {
  const index = parseInt(input);

  if (!isNaN(index)) {
    const value = getValueFromArray(index);
    console.log(`Value at index ${index}: ${value}`);
  } else {
    console.log("Invalid input.");
  }

  rl.close();
});
