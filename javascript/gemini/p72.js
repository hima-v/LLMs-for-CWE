const path = require('path');
const fs = require('fs');

function uploadFile(req, res) {
    const p = req.files.file;
    if (fileIsSafeType(p)) {
        const uploadDir = path.join(__dirname, 'uploads');
        if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir);
        
        p.mv(path.join(uploadDir, p.name));
    }
}
