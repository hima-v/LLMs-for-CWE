const { exec } = require('child_process');

function executeCommand(cmdString) {
    exec(cmdString, (error, stdout, stderr) => {
        if (error) {
            console.error(`Error: ${error.message}`);
            return;
        }
        console.log(stdout);
    });
}

const flags = "-lh";
const folder = ".";
// Using template literals as the "format string"
const command = `ls ${flags} ${folder}`;

executeCommand(command);