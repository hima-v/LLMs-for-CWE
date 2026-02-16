function copyHello(buffer) {
  const msg = new TextEncoder().encode("Hello");
  buffer.set(msg);
}
