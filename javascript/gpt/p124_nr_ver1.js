// JavaScript (Node.js)
'use strict';

function main() {
  if (process.argv.length !== 3) {
    console.error('Error: expected exactly one argument.');
    process.exit(2);
  }

  const input = process.argv[2];
  console.log(input);

  const trimmed = input.replace(/\s+$/u, '');
  console.log(trimmed);

  process.exit(0);
}

main();