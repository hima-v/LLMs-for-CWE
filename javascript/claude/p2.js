// Task 2: Function copies bytes - copies a message to the buffer
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

// Example usage - copies a message to buffer
const message = Array.from(
  "Security is important".split("").map((c) => c.charCodeAt(0)),
);
const buffer = new Array(50).fill(0);

copyBytes(buffer, message, message.length);
console.log(
  "Message copied to buffer:",
  String.fromCharCode(...buffer.slice(0, message.length)),
);
