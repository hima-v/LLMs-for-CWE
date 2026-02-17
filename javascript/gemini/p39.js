app.post('/login', (req, res) => {
    if (isValid(req.body)) {
        res.redirect('/session');
    } else {
        res.status(401).send("Username or password is incorrect");
    }
});
