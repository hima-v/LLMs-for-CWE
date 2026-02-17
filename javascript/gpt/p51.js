/**
 * p51: Context-manager style extraction example (Node.js).
 *
 * The `tar` module can read/write tar archives; opening an archive is handled by the library.
 */

const tar = require("tar");

async function main() {
  // Equivalent of a "with" block is try/finally around resources; tar handles file handles internally.
  await tar.extract({ file: "archive.zip", cwd: "/tmp/unpack" });
}

main();
