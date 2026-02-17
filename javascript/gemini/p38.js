app.post('/login', (req, res) => {
    const { username, password } = req.body;
    auth.check(username, password, (isValid) => {
        res.redirect(isValid ? '/dashboard' : '/login');
    });
});
