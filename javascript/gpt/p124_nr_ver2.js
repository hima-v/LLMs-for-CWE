'use strict';

function rtrim(str) {
  let i = str.length;
  while (i > 0) {
    const ch = str.charCodeAt(i - 1);
    const isSpace =
      ch === 0x20 || ch === 0x09 || ch === 0x0A || ch === 0x0D || ch === 0x0B || ch === 0x0C;
    if (!isSpace) break;
    i--;
  }
  return str.slice(0, i);
}

function main() {
  const argv = process.argv.slice(2);
  if (argv.length !== 1) {
    console.error('Error: expected exactly one argument.');
    process.exit(1);
  }

  const input = argv[0];
  console.log(input);

  const trimmed = rtrim(input);
  console.log(trimmed);

  process.exit(0);
}

main();