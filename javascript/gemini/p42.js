app.get('/info', (req, res) => {
    const s = req.session;
    res.send(`${s.username}; ${s.email}; ${s.ssn}`);
});
