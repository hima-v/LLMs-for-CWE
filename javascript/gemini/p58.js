app.post('/change-email', (req, res) => {
    if (!req.session.userId) return res.redirect('/login');

    const { oldEmail, newEmail, password } = req.body;
    
    db.query('SELECT * FROM users WHERE id = ?', [req.session.userId], (err, results) => {
        const user = results[0];
        if (user.email === oldEmail && verifyHash(password, user.password_hash)) {
            db.query('UPDATE users SET email = ? WHERE id = ?', [newEmail, user.id]);
            res.send("Email changed");
        } else {
            res.send("Wrong email or password");
        }
    });
});
