const fs = require("fs");

function getValueFromArray(arr, size, index) {
  return index >= 0 && index < size ? arr[index] : -1;
}

const id_sequence = [100, 200, 300];
const input = parseInt(fs.readFileSync(0).toString());
process.exit(getValueFromArray(id_sequence, id_sequence.length, input));
