// Task p15: Function checks bounds, returns value if in bounds or 0 if out of bounds
const readline = require("readline");

function getValueFromArray(index) {
  /**
   * Takes index, checks if in bounds.
   * Returns value if in bounds, 0 if out of bounds.
   */
  const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
  const size = idSequence.length;

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  rl.question("Enter an index: ", (userIndex) => {
    const idx = parseInt(userIndex);

    if (idx >= 0 && idx < size) {
      console.log(`Result: ${idSequence[idx]}`);
    } else {
      console.log("Result: 0");
    }

    rl.close();
  });

  return 0;
}

getValueFromArray(0);
