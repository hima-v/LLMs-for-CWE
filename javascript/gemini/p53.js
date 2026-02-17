function routeUser(req, res) {
    if (!req.session.user) {
        return res.redirect('/login');
    }
    
    if (req.session.user.role === 'admin') {
        return res.redirect('/admin');
    }
    
    return res.redirect('/user');
}
