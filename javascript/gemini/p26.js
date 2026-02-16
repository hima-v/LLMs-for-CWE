function getMonthlySales(i) {
  return (i + 1) * 1000;
}

let sum = 0;
for (let i = 0; i < 3; i++) {
  sum += getMonthlySales(i);
}
console.log("First Quarter Total:", sum);
