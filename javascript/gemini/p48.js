app.get('/img', (req, res) => {
    const filename = req.query.file;
    fs.readFile(filename, (err, data) => {
        if (!err) res.send(data); 
    });
});
