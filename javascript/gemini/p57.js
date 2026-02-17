app.post('/update_email', (req, res) => {
    if (!req.session.userId) return res.redirect('/login');
    
    const { oldEmail, newEmail, confirmPassword } = req.body;
    
    // Check if user owns the old email and password is correct
    db.query("SELECT * FROM users WHERE id=? AND email=? AND password=?", 
        [req.session.userId, oldEmail, confirmPassword], 
        (err, results) => {
            if (results.length > 0) {
                db.query("UPDATE users SET email=? WHERE id=?", [newEmail, req.session.userId]);
                res.send("Email changed");
            } else {
                res.send("Invalid details");
            }
        });
});
