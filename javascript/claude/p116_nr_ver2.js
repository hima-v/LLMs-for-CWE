const { execFileSync } = require("child_process");

try {
    const output = execFileSync("ls", [], { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"] });
    console.log(output);
} catch (error) {
    console.error("Error:", error.message);
    process.exit(1);
}