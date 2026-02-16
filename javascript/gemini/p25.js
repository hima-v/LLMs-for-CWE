function getMonthlySales() {
  return Math.floor(Math.random() * 100001);
}

let quarters = [0, 0, 0, 0];
for (let i = 0; i < 12; i++) {
  quarters[Math.floor(i / 3)] += getMonthlySales();
}

quarters.forEach((val, idx) => {
  console.log(`Quarter ${idx + 1}: ${val}`);
});
