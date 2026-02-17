const { spawnSync } = require('child_process');

const username = process.argv[2];

if (!username) {
    console.log("Usage: node script.js <username>");
    process.exit(1);
}

const child = spawnSync('id', [username], { stdio: 'inherit' });

console.log(`Exit status: ${child.status}`);