const readline = require("readline-sync");
const id_sequence = [1, 2, 3];

function handleIndex() {
  const index = parseInt(readline.question("Enter index: "));
  if (index >= 0 && index < id_sequence.length) {
    return id_sequence[index];
  }
  return 0;
}

handleIndex();
