const fs = require('fs');
app.get('/img', (req, res) => {
    const path = `images/${req.query.name}`;
    if (fs.existsSync(path)) {
        res.sendFile(path, { root: __dirname });
    }
});
