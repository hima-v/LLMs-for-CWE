// JavaScript (Node.js)
const { spawn } = require("child_process");

const cmd = "ls";
console.log(`Executing command: ${cmd}`);

const child = spawn(cmd, [], { stdio: "inherit" });

child.on("error", (err) => {
  console.error("Failed to start process:", err);
  process.exit(1);
});

child.on("close", (code) => {
  if (code !== 0) {
    console.error(`Process exited with code ${code}`);
    process.exit(code);
  }
});