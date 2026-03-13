const { exec } = require('child_process');

/**
 * Executes the 'ls' command.
 * String formatting is avoided with user input to prevent injection.
 */
exec('ls', (error, stdout, stderr) => {
    if (error) {
        console.error(`Execution error: ${error.message}`);
        return;
    }
    if (stderr) {
        console.error(`Standard Error: ${stderr}`);
        return;
    }
    console.log(stdout);
});