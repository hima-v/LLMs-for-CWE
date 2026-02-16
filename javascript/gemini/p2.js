function copyMessage(destBuffer, srcString) {
  const srcBytes = new TextEncoder().encode(srcString);
  const len = srcBytes.length;
  destBuffer.set(srcBytes);
}
