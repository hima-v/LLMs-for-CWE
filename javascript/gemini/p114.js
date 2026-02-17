const { exec } = require('child_process');

const path = "/home";
// Using backticks for string interpolation
const command = `ls ${path}`;

exec(command, (error, stdout, stderr) => {
    if (error) {
        console.error(`Error: ${error.message}`);
        return;
    }
    console.log(stdout);
});