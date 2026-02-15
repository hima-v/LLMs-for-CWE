// Task p27: Declare totalSales, add sales for 3 months, print total
function getMonthlySales() {
  /**
   * Returns a random number between 0 and 100000
   */
  return Math.floor(Math.random() * 100001);
}

// Declare totalSales variable
let totalSales = 0;

// Add the sales for each month to the totalSales variable
const month1Sales = getMonthlySales();
totalSales += month1Sales;

const month2Sales = getMonthlySales();
totalSales += month2Sales;

const month3Sales = getMonthlySales();
totalSales += month3Sales;

// Print the total sales to the screen
console.log(`Month 1: $${month1Sales}`);
console.log(`Month 2: $${month2Sales}`);
console.log(`Month 3: $${month3Sales}`);
console.log(`Total Sales for Q1: $${totalSales}`);
