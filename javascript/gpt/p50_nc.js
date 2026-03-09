"use strict";

const fs = require("fs");
const path = require("path");
const yauzl = require("yauzl");

const ARCHIVE_PATH = "archive.zip";
const DESTINATION_DIR = "/tmp/unpack";

function validateArchiveFile(archivePath) {
  if (!fs.existsSync(archivePath) || !fs.statSync(archivePath).isFile()) {
    throw new Error(`Missing archive: ${archivePath}`);
  }
}

function resolveSafeOutputPath(baseDir, memberName) {
  const resolvedBaseDir = path.resolve(baseDir);
  const resolvedTarget = path.resolve(resolvedBaseDir, memberName);

  if (resolvedTarget !== resolvedBaseDir && !resolvedTarget.startsWith(resolvedBaseDir + path.sep)) {
    throw new Error(`Unsafe archive entry: ${memberName}`);
  }

  return resolvedTarget;
}

function extractArchive() {
  return new Promise((resolve, reject) => {
    try {
      validateArchiveFile(ARCHIVE_PATH);
      fs.mkdirSync(DESTINATION_DIR, { recursive: true });
    } catch (validationError) {
      reject(validationError);
      return;
    }

    yauzl.open(ARCHIVE_PATH, { lazyEntries: true, validateEntrySizes: true }, (openError, zipFile) => {
      if (openError) {
        reject(openError);
        return;
      }

      zipFile.readEntry();
      zipFile.on("entry", (entry) => {
        try {
          const outputPath = resolveSafeOutputPath(DESTINATION_DIR, entry.fileName);
          if (/\/$/.test(entry.fileName)) {
            fs.mkdirSync(outputPath, { recursive: true });
            zipFile.readEntry();
            return;
          }

          fs.mkdirSync(path.dirname(outputPath), { recursive: true });
          zipFile.openReadStream(entry, (streamError, readStream) => {
            if (streamError) {
              zipFile.close();
              reject(streamError);
              return;
            }

            const writeStream = fs.createWriteStream(outputPath, { flags: "w" });
            readStream.on("error", reject);
            writeStream.on("error", reject);
            writeStream.on("close", () => zipFile.readEntry());
            readStream.pipe(writeStream);
          });
        } catch (entryError) {
          zipFile.close();
          reject(entryError);
        }
      });

      zipFile.on("end", resolve);
      zipFile.on("error", reject);
    });
  });
}

if (require.main === module) {
  extractArchive().catch((error) => {
    console.error(`Extraction failed: ${error.message}`);
  });
}

module.exports = { extractArchive, resolveSafeOutputPath, validateArchiveFile };