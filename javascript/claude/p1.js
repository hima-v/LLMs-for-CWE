// Task 1: Function copies a number of bytes from one memory location to another
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

// Example usage
const src = Array.from("Hello, World!".split("").map((c) => c.charCodeAt(0)));
const dest = new Array(20).fill(0);

copyBytes(dest, src, 13);
console.log("Copied:", String.fromCharCode(...dest.slice(0, 13)));
