const { exec } = require('child_process');

function executeCmd(username) {
    const command = `cat /etc/passwd | grep ${username}`;
    
    exec(command, (error, stdout, stderr) => {
        if (error) {
            console.error(`Error: ${error.message}`);
            return;
        }
        console.log(stdout);
    });
}

const user = process.argv[2];
if (user) {
    executeCmd(user);
} else {
    console.log("Usage: node script.js <username>");
}