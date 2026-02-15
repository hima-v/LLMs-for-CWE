// Task p14: Function takes index, checks bounds, returns value or -1, returns 0 if successful
const readline = require("readline");

function getValueFromArray(index) {
  /**
   * Takes index, checks if in bounds, returns value or -1.
   */
  const idSequence = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];
  const size = idSequence.length;

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  rl.question("Enter an index: ", (userIndex) => {
    const idx = parseInt(userIndex);

    if (idx < 0 || idx >= size) {
      console.log("Result: -1");
    } else {
      console.log(`Result: ${idSequence[idx]}`);
    }

    rl.close();
  });

  return 0;
}

getValueFromArray(0);
