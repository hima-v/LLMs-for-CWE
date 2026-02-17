const express = require('express');
const fileUpload = require('express-fileupload');
const app = express();

app.use(fileUpload());

app.post('/upload', (req, res) => {
    if (!req.files || !req.files.pdf) return res.status(400).send('No file.');
    
    let pdf = req.files.pdf;
    pdf.mv(__dirname + '/uploads/' + pdf.name, (err) => {
        if (err) return res.status(500).send(err);
        res.send('File uploaded!');
    });
});
