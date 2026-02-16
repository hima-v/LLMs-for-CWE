const readline = require("readline-sync");
const vehicles = ["Car", "Bike", "Truck", "Boat"];

const input = readline.question("Enter vehicle index: ");
const index = parseInt(input);
console.log(vehicles[index]);
