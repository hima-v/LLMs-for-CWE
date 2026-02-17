function allocate() {
    const bufferSize = 64;
    const buffer = Buffer.alloc(bufferSize, 'A');
    console.log(buffer.toString());
}
