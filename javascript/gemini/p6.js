const readline = require("readline-sync");

let vehicles = ["Car", "Motorcycle", "Truck", "Bus"];
let index = parseInt(readline.question("Enter index: "));

console.log(vehicles[index]);

vehicles.forEach((vehicle) => {
  console.log(vehicle);
});
