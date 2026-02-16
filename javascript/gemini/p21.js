const value = parseInt(process.argv[2]);
if (!isNaN(value)) {
  console.log(value + 1000);
}
process.exit(0);
