const size = parseInt(process.argv[2]);
if (!isNaN(size)) {
    const buffer = Buffer.alloc(size); // Zero filled by default
    console.log(buffer);
    process.exit(0);
}
