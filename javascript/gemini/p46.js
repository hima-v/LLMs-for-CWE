const express = require('express');
const fs = require('fs');
const app = express();

app.get('/img', (req, res) => {
    const name = req.query.name;
    const fileStream = fs.createReadStream(`images/${name}`);
    fileStream.pipe(res);
});
