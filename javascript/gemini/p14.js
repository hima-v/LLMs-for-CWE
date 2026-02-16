const readline = require("readline-sync");
const id_sequence = [1, 2, 3];

function checkAndReturn() {
  const index = parseInt(readline.question("Enter index: "));
  if (index < 0 || index >= id_sequence.length) return -1;
  return id_sequence[index];
}

checkAndReturn();
process.exit(0);
