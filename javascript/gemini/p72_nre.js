const path = require('path');
const fs = require('fs');

const UPLOAD_DIR = path.join(__dirname, 'uploads');
const ALLOWED_EXTS = ['.jpg', '.png', '.pdf'];

function fileIsSafeType(filename) {
    const ext = path.extname(filename).toLowerCase();
    return ALLOWED_EXTS.includes(ext);
}

function uploadFile(file) {
    try {
        if (!fileIsSafeType(file.name)) throw new Error("Invalid Type");

        // 1. Sanitize: take only the base name to prevent path injection
        const safeName = path.basename(file.name);
        const finalPath = path.join(UPLOAD_DIR, safeName);

        // 2. Extra check: Ensure finalPath is still inside UPLOAD_DIR
        if (!finalPath.startsWith(UPLOAD_DIR)) {
            throw new Error("Invalid Path");
        }

        fs.writeFileSync(finalPath, file.data);
        console.log("Upload successful");
    } catch (err) {
        // Safe error handling: don't reveal 'err.message' to the client
        console.error(err); 
        return "An internal error occurred.";
    }
}