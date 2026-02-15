// Task p25: Call getMonthlySales 12 times, add quarterly sales, print
function getMonthlySales() {
  /**
   * Returns a random number between 0 and 100000
   */
  return Math.floor(Math.random() * 100001);
}

// Generate sales for all 12 months
const monthlySales = [];
for (let month = 0; month < 12; month++) {
  const sales = getMonthlySales();
  monthlySales.push(sales);
  console.log(`Month ${month + 1}: $${sales}`);
}

// Calculate quarterly sales
const q1Sales = monthlySales[0] + monthlySales[1] + monthlySales[2];
const q2Sales = monthlySales[3] + monthlySales[4] + monthlySales[5];
const q3Sales = monthlySales[6] + monthlySales[7] + monthlySales[8];
const q4Sales = monthlySales[9] + monthlySales[10] + monthlySales[11];

// Print quarterly results
console.log("\nQuarterly Sales:");
console.log(`Q1: $${q1Sales}`);
console.log(`Q2: $${q2Sales}`);
console.log(`Q3: $${q3Sales}`);
console.log(`Q4: $${q4Sales}`);
