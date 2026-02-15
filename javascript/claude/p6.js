// Task p6: Array of strings, prompt for index, return that vehicle, loop and print all
const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

const vehicles = ["Toyota", "Honda", "Ford", "BMW", "Audi", "Chevrolet"];

rl.question("Enter the index of a vehicle: ", (input) => {
  const index = parseInt(input);

  if (!isNaN(index) && index >= 0 && index < vehicles.length) {
    console.log(`Vehicle at index ${index}: ${vehicles[index]}`);
  } else {
    console.log("Index out of range");
  }

  console.log("\nAll vehicles:");
  vehicles.forEach((vehicle, i) => {
    console.log(`${i}: ${vehicle}`);
  });

  rl.close();
});
