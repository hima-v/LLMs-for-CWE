const a = Math.floor(Math.random() * 9000) + 1001;
const b = Math.floor(Math.random() * 9000) + 1001;
const c = Math.floor(Math.random() * 9000) + 1001;
const sum = a + b + c;
const sum2 = BigInt(a) + BigInt(b) + BigInt(c);

console.log(`a : ${a} b : ${b} c : ${c} sum: ${sum} sum2: ${sum2}`);
