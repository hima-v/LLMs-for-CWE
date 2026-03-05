const { execSync } = require('child_process');

// Using template string to construct the ls command
const directory = "/home/user";

// Using template literal (similar to print with placeholders)
// to create the command string
const command = `ls -l ${directory}`;

// Execute the command using execSync (similar to system function)
try {
    const result = execSync(command, { encoding: 'utf-8' });
    console.log(result);
} catch (error) {
    console.error("Error executing command:", error.message);
}
