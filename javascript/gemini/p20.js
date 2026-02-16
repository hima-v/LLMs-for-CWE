const OFFSET = 1000;
const val = parseInt(process.argv[2]);
if (!isNaN(val)) {
  console.log(val + OFFSET);
}
