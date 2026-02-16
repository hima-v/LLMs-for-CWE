const val = parseInt(process.argv[2]);
if (!isNaN(val)) {
  console.log(val + 1000);
}
