const fs = require('fs');

const data = 'important_config';
// 'w' flag in Node usually combines O_WRONLY | O_CREAT | O_TRUNC
// Using constants for exact behavior:
const flags = fs.constants.O_WRONLY | fs.constants.O_CREAT;
const mode = 0o600; // S_IRUSR | S_IWUSR

fs.writeFile('important_config', data, { flag: flags, mode: mode }, (err) => {
    if (err) throw err;
    console.log('File saved!');
});