// JavaScript (Node.js/Express Example)
const express = require('express');
const app = express();
const db = require('./db'); // Hypothetical db pool

app.post('/buy', async (req, res) => {
    // Session-based user ID
    const username = req.session.user_id;
    const { stock_name, stock_quantity } = req.body;

    const allowedStocks = ['AAPL', 'GOOGL', 'MSFT', 'TSLA'];
    const qty = parseInt(stock_quantity);

    // Validation
    if (!allowedStocks.includes(stock_name) || isNaN(qty) || qty <= 0 || qty > 1000) {
        return res.status(400).send('Invalid request');
    }

    try {
        // Parameterized query via pg/mysql driver
        await db.query(
            'INSERT INTO orders (username, stock, quantity) VALUES ($1, $2, $3)',
            [username, stock_name, qty]
        );
        res.redirect('/profile');
    } catch (err) {
        res.status(500).send('Server Error');
    }
});