/**
 * JavaScript (Node.js + Express) - Stock Buy Order Handler
 * Simple example of form submission and database insertion
 */

const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
const PORT = 3000;

// Middleware
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));
app.set('view engine', 'ejs');

// Initialize SQLite database
const db = new sqlite3.Database(':memory:');

// Create orders table
db.serialize(() => {
    db.run(`
        CREATE TABLE IF NOT EXISTS orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            stock_name TEXT NOT NULL,
            quantity INTEGER NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    `);
});

// Route: Display the form
app.get('/', (req, res) => {
    res.render('form');
});

// Route: Handle form submission and insert into database
app.post('/buy_order', (req, res) => {
    const { stock_name, quantity } = req.body;
    
    // Validate input
    if (!stock_name || !quantity) {
        return res.status(400).send('Error: Missing stock name or quantity');
    }
    
    const qty = parseInt(quantity);
    if (isNaN(qty) || qty <= 0) {
        return res.status(400).send('Error: Quantity must be a positive number');
    }
    
    // Insert into database
    db.run(
        'INSERT INTO orders (stock_name, quantity) VALUES (?, ?)',
        [stock_name, qty],
        function(err) {
            if (err) {
                return res.status(500).send('Database error: ' + err.message);
            }
            // Redirect to stock view page
            res.redirect('/stock_view');
        }
    );
});

// Route: Display all stock orders
app.get('/stock_view', (req, res) => {
    db.all(
        'SELECT id, stock_name, quantity, created_at FROM orders ORDER BY created_at DESC',
        (err, rows) => {
            if (err) {
                return res.status(500).send('Database error: ' + err.message);
            }
            res.render('stock_view', { orders: rows || [] });
        }
    );
});

// Start server
app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});

// Graceful shutdown
process.on('SIGINT', () => {
    db.close();
    process.exit();
});