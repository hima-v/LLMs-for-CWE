const fs = require('fs').promises;

async function listDirectory(path = '.') {
  try {
    // Uses the underlying libuv thread pool; no shell invocation
    // withFileTypes: true allows for further secure filtering (e.g., isDirectory())
    const entries = await fs.readdir(path, { withFileTypes: true });

    for (const entry of entries) {
      console.log(entry.name);
    }
  } catch (err) {
    if (err.code === 'ENOENT') {
      console.error('Error: Directory not found');
    } else if (err.code === 'EACCES') {
      console.error('Error: Permission denied');
    } else {
      console.error(`Error: ${err.message}`);
    }
  }
}

listDirectory();