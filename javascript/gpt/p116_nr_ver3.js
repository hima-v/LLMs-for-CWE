'use strict';

const { spawn } = require('node:child_process');

const cmd = 'ls';
process.stdout.write(`Executing: ${cmd}\n`);

const child = spawn('ls', [], { stdio: 'inherit' });

child.on('error', (err) => {
  console.error(`spawn failed: ${err.message}`);
  process.exit(127);
});

child.on('close', (code, signal) => {
  if (signal) {
    console.error(`ls terminated by signal: ${signal}`);
    process.exit(128);
  }
  if (code !== 0) {
    console.error(`ls failed with exit code ${code}`);
    process.exit(code ?? 1);
  }
  process.exit(0);
});