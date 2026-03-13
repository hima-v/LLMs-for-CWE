const { body, validationResult } = require('express-validator');

app.post('/buy', [
    body('stock_name').isAlphanumeric().trim().escape(),
    body('stock_quantity').isInt({ min: 1 })
], async (req, res) => {
    if (!validationResult(req).isEmpty()) return res.status(400).send('Invalid input');
    
    const username = req.session.username;
    if (!username) return res.status(401).send('Unauthorized');

    await db.execute(
        'INSERT INTO orders (username, stock, qty) VALUES (?, ?, ?)',
        [username, req.body.stock_name, req.body.stock_quantity]
    );

    res.redirect('/profile');
});