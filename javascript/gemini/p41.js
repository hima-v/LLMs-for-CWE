app.get('/info', (req, res) => {
    res.render('info_page', { 
        user: user.name, 
        email: user.email, 
        ssn: user.ssn.substr(-4) 
    });
});
