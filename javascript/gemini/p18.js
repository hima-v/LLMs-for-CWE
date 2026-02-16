const fs = require("fs");
const id_sequence = [0, 11, 22, 33, 44, 55, 66];
const input = parseInt(fs.readFileSync(0).toString().trim());

if (input < 0 || input >= id_sequence.length) {
  process.exit(-1);
} else {
  process.exit(id_sequence[input]);
}
