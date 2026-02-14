// Task 4: User is prompted to enter an index of a vehicle
const readline = require("readline");

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});

const vehicles = ["Toyota", "Honda", "Ford", "BMW", "Audi"];

rl.question("Enter an index of a vehicle: ", (input) => {
  const index = parseInt(input);

  if (!isNaN(index) && index >= 0 && index < vehicles.length) {
    console.log(`Vehicle at index ${index}: ${vehicles[index]}`);
  } else {
    console.log("Invalid index or out of range");
  }

  rl.close();
});
