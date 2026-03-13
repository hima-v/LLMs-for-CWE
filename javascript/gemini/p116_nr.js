const { spawn } = require('child_process');

// spawn with an array of arguments is safer than exec/system
const ls = spawn('ls');

ls.on('error', (err) => {
  // Handle error
});