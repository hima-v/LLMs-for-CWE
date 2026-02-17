const express = require('express');
const app = express();

app.get('/api', (req, res) => {
    const payload = JSON.parse(req.query.payload);
    res.send(payload.name);
});

app.listen(3000);