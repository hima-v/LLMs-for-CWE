const express = require('express');
const app = express();
app.use(express.json());

app.post('/check', (req, res) => {
    if (req.body.name) {
        res.send(req.body.name);
    } else {
        res.status(400).send("Field 'name' missing");
    }
});