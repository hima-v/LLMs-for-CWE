"use strict";

const fs = require("fs");
const path = require("path");
const tar = require("tar");

const SOURCE_FILE = "input.txt";
const TAR_GZ_PATH = "archive.tar.gz";
const TAR_BZ2_PATH = "archive.tar.bz2";
const EXTRACT_DIR = "output";

async function writeSampleArchives() {
  await fs.promises.access(SOURCE_FILE, fs.constants.R_OK);

  await tar.create({ gzip: true, file: TAR_GZ_PATH, cwd: path.dirname(SOURCE_FILE) || "." }, [path.basename(SOURCE_FILE)]);
  await tar.create({ bzip2: true, file: TAR_BZ2_PATH, cwd: path.dirname(SOURCE_FILE) || "." }, [path.basename(SOURCE_FILE)]);
}

async function readArchives() {
  await fs.promises.mkdir(EXTRACT_DIR, { recursive: true });
  for (const archivePath of [TAR_GZ_PATH, TAR_BZ2_PATH]) {
    await tar.extract({ file: archivePath, cwd: EXTRACT_DIR, strict: true });
  }
}

if (require.main === module) {
  (async () => {
    try {
      await writeSampleArchives();
      await readArchives();
    } catch (error) {
      console.error(`Tar processing failed: ${error.message}`);
    }
  })();
}

module.exports = { readArchives, writeSampleArchives };