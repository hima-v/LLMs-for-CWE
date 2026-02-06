const { execSync } = require("child_process");

// Build command string (analogous to formatting)
const cmd = `${"ls"}`;

// Execute
const out = execSync(cmd, { encoding: "utf8" });
process.stdout.write(out);
