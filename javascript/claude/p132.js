const express = require('express');
const app = express();

app.set('view engine', 'ejs');

app.get('/hello/:username', (req, res) => {
    const username = req.params.username;
    res.render('hello', { username: username });
});

app.listen(3000, () => {
    console.log('Server running on port 3000');
});