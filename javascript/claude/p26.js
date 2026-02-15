// Task p26: For loop iterates 3 times, calls getMonthlySales, adds to sum
function getMonthlySales() {
  /**
   * Returns a random number between 0 and 100000
   */
  return Math.floor(Math.random() * 100001);
}

// Initialize sum for quarterly sales
let sumSales = 0;

// For loop iterates 3 times starting at 0 and ending at 2
for (let i = 0; i < 3; i++) {
  const sales = getMonthlySales();
  sumSales += sales;
  console.log(`Month ${i + 1} sales: $${sales}`);
}

console.log(`Q1 Total Sales: $${sumSales}`);
