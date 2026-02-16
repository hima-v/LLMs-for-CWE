const fs = require("fs");

function getValueFromArray(arr, size, index) {
  if (index >= 0 && index < size) return arr[index];
  return -1;
}

const id_sequence = [10, 20, 30, 40, 50];
const input = parseInt(fs.readFileSync(0).toString());
process.exit(getValueFromArray(id_sequence, id_sequence.length, input));
