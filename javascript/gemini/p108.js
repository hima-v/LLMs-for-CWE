const { exec } = require('child_process');

const username = 'root';

// exec handles the shell execution and returns the output in a callback
const output = exec(`getent passwd ${username}`, (error, stdout, stderr) => {
    if (error) {
        console.error(`Error: ${error.message}`);
        return;
    }
    if (stderr) {
        console.error(`Stderr: ${stderr}`);
        return;
    }
    console.log(`User Info: ${stdout}`);
});