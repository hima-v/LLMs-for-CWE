const express = require('express');
const fileUpload = require('express-fileupload');
const app = express();

app.use(fileUpload());

app.post('/upload', (req, res) => {
    if (!req.files || !req.files.image) return res.sendStatus(400);
    
    const image = req.files.image;
    const base64Data = image.data.toString('base64');
    
    // Mock DB call
    db.query("INSERT INTO images (name, data) VALUES (?, ?)", [image.name, base64Data], (err) => {
        if (err) throw err;
        res.send("Saved to DB");
    });
});
