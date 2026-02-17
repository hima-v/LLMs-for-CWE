app.post('/change_email', (req, res) => {
    if (!req.session.user) return res.status(403).send("Login required");

    const { oldEmail, newEmail, password } = req.body;
    
    // Verify password & old email logic here
    if (newEmail === oldEmail) {
        return res.send("New email must be different");
    }

    db.updateEmail(req.session.user.id, newEmail, password, (err) => {
        if (err) res.send("Error or Wrong Password");
        else res.send("Success");
    });
});
