// Task 3: Function copies bytes - copies the string "Hello" to the buffer
function copyBytes(dest, src, numBytes) {
  /**
   * Copies numBytes from src to dest.
   * dest: destination array
   * src: source array
   * numBytes: number of bytes to copy
   */
  for (let i = 0; i < numBytes; i++) {
    dest[i] = src[i];
  }
}

// Example usage - copies "Hello" to buffer
const hello = Array.from("Hello".split("").map((c) => c.charCodeAt(0)));
const buffer = new Array(20).fill(0);

copyBytes(buffer, hello, 5);
console.log("Copied to buffer:", String.fromCharCode(...buffer.slice(0, 5)));
