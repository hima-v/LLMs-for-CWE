const fs = require('fs');
const zlib = require('zlib');

// JavaScript doesn't have a direct 'with' for files, 
// but streams and callbacks handle the context.
const gzip = zlib.createGzip();
const inp = fs.createReadStream('input.txt');
const out = fs.createWriteStream('input.txt.gz');

inp.pipe(gzip).pipe(out).on('finish', () => {
    console.log("Compression complete");
});
