function copyBytes(dest, src, n) {
  // Using Uint8Array to simulate memory buffers
  dest.set(src.subarray(0, n));
}
