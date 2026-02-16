const fs = require("fs");

function getValueFromArray(arr, size, index) {
  if (index < 0 || index >= size) {
    return -1;
  }
  return arr[index];
}

let id_sequence = [100, 200, 300, 400];
// Reading from stdin
let input = parseInt(fs.readFileSync(0).toString());

let result = getValueFromArray(id_sequence, id_sequence.length, input);
process.exit(result);
