const readline = require("readline-sync");
const id_sequence = [5, 10, 15];

function getVal(index) {
  if (index < 0 || index >= id_sequence.length) return -1;
  return id_sequence[index];
}

const userIdx = parseInt(readline.question("Enter index: "));
getVal(userIdx);
