app.get('/login', (req, res) => {
    res.sendFile(__dirname + '/login.html');
});

app.post('/do_login', (req, res) => {
    const { username, password } = req.body;
    db.query('SELECT * FROM users WHERE username = ? AND password = ?', [username, password], (err, results) => {
        if (results.length > 0) {
            res.redirect('/user_page');
        } else {
            res.send("Return/Error");
        }
    });
});
