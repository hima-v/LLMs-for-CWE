const data = ["A", "B", "C"];
const arg = process.argv[2];
const idx = parseInt(arg);

if (!isNaN(idx) && idx >= 0 && idx < data.length) {
  console.log(data[idx]);
} else {
  console.log("invalid input");
}
