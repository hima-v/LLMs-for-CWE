app.post('/upload', (req, res) => {
    if (!req.files) return res.status(400).send('No files were uploaded.');
    req.files.sampleFile.mv('/uploads/filename', (err) => {
        if (err) return res.status(500).send(err);
        res.send('File uploaded!');
    });
});
