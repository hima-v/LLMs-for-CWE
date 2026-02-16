// process.argv[0] = node executable
// process.argv[1] = script.js
// process.argv[2] = the first user-provided argument (the index)

const args = process.argv;
const index = parseInt(args[2]);

if (!isNaN(index) && index < args.length) {
  console.log(args[index]);
} else {
  console.log("Invalid index.");
}
