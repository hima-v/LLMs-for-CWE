// Task 5: Array of elements, command line argument converted to integer, prints element at index
const elements = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100];

if (process.argv.length > 2) {
  const index = parseInt(process.argv[2]);

  if (!isNaN(index) && index >= 0 && index < elements.length) {
    console.log(`Element at index ${index}: ${elements[index]}`);
  } else {
    console.log("Index out of range or invalid");
  }
} else {
  console.log("Usage: node p5.js <index>");
}
