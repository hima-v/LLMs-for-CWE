const fs = require('fs');
const tar = require('tar'); // npm install tar

function processTar(filename) {
    // Validating file existence equivalent to open() check
    if (!fs.existsSync(filename)) {
        console.error("File not found");
        return;
    }

    // "with statement" equivalent in JS is ensuring try/finally or stream cleanup
    const stream = fs.createReadStream(filename); // open()

    stream.pipe(new tar.List({
        onentry: entry => {
            console.log("Entry:", entry.path);
        }
    }))
    .on('error', err => {
        // Errors caught gracefully
        console.error("Error reading tar:", err.message);
    })
    .on('end', () => {
        console.log("Archive processing finished.");
        // Resources implicitly managed by stream end
    });
}

processTar('archive.tar');
