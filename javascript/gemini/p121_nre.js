const size = 10;
let chunk1 = new Int32Array(size);
let chunk2 = new Int32Array(size);

for (let i = 0; i < size; i++) {
    chunk1[i] = i;
}

chunk2.set(chunk1);

console.log(chunk2);

chunk1 = null;

console.log(chunk2);

chunk2 = null;