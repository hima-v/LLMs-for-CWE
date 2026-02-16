/**
 * p49: Extract archive.zip contents into /tmp/unpack.
 *
 * Uses the `tar` npm package (supports many archive types).
 */

const tar = require("tar");

async function main() {
  await tar.extract({ file: "archive.zip", cwd: "/tmp/unpack" });
}

main();
