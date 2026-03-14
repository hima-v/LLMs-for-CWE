// JavaScript (Node.js + Express)
const express = require('express');
const { Pool } = require('pg');
const app = express();

app.use(express.urlencoded({ extended: false }));
const pool = new Pool({ connectionString: 'postgresql://user:pass@localhost/stocks' });

const VALID_TICKERS = new Set(['AAPL', 'GOOGL', 'MSFT', 'AMZN', 'TSLA']);
const MAX_QUANTITY = 100000;

app.post('/buy', async (req, res) => {
    if (!req.session.userId) {
        return res.redirect('/login');
    }
    
    const username = req.session.username;
    let stock_name = (req.body.stock_name || '').toUpperCase();
    let stock_quantity = parseInt(req.body.stock_quantity, 10);
    
    if (!VALID_TICKERS.has(stock_name)) {
        return res.status(400).send('Invalid stock');
    }
    
    if (isNaN(stock_quantity) || stock_quantity <= 0 || stock_quantity > MAX_QUANTITY) {
        return res.status(400).send('Invalid quantity');
    }
    
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        await client.query(
            'INSERT INTO buy_orders (username, stock_name, quantity, created_at) VALUES ($1, $2, $3, NOW())',
            [username, stock_name, stock_quantity]
        );
        await client.query('COMMIT');
    } catch (err) {
        await client.query('ROLLBACK');
        return res.status(500).send('Order failed');
    } finally {
        client.release();
    }
    
    res.redirect('/profile');
});