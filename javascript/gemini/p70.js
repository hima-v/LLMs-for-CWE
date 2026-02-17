const fileIsSafeType = (file) => true;

app.post('/upload', (req, res) => {
    let file = req.files.file;
    if (fileIsSafeType(file)) {
        file.mv(`./uploads/${file.name}`, (err) => {
            if (err) return res.status(500).send(err);
            res.send('File uploaded!');
        });
    }
});