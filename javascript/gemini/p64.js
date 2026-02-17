const multer = require('multer');
const upload = multer({ dest: 'uploads/' });

app.post('/upload_pdf', upload.single('pdf'), (req, res) => {
    const filepath = req.file.path;
    db.query("INSERT INTO documents (filepath) VALUES (?)", [filepath], (err) => {
        if(err) throw err;
        res.send("Uploaded and saved to DB");
    });
});
