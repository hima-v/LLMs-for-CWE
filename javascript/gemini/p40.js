app.get('/info', (req, res) => {
    const { username, email, ssn } = req.session;
    res.send(`Username: ${username}, Email: ${email}, SSN: ${ssn.slice(-4)}`);
});
