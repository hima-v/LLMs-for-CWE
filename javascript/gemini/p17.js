const fs = require("fs");
const id_sequence = [10, 20, 30, 40, 50];
const input = parseInt(fs.readFileSync(0).toString().trim());

if (input < 0 || input >= id_sequence.length) {
  process.exit(-1);
} else {
  process.exit(id_sequence[input]);
}
