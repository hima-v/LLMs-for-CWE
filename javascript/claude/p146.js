/**
 * JavaScript (Node.js + Express) - Stock Buy Order Processing
 * Install dependencies: npm install express body-parser sqlite3
 */

const express = require('express');
const bodyParser = require('body-parser');
const sqlite3 = require('sqlite3').verbose();
const session = require('express-session');
const path = require('path');

const app = express();
const PORT = 3000;

// Middleware
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(session({
    secret: 'your-secret-key',
    resave: false,
    saveUninitialized: true
}));

// Database initialization
const db = new sqlite3.Database('stocks.db', (err) => {
    if (err) console.error('Database error:', err);
    else console.log('Connected to database');
});

// Create tables
db.run(`
    CREATE TABLE IF NOT EXISTS buy_orders (
        order_id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL,
        stock_name TEXT NOT NULL,
        stock_quantity INTEGER NOT NULL,
        order_date DATETIME DEFAULT CURRENT_TIMESTAMP
    )
`);

/**
 * POST /buy-stock
 * Handles stock purchase form submission
 * Expects: stock_name, stock_quantity from form data
 */
app.post('/buy-stock', (req, res) => {
    // Extract form data
    const stock_name = req.body.stock_name?.trim();
    const stock_quantity = req.body.stock_quantity?.trim();
    
    // Get username from session
    const username = req.session.username;
    
    // Validation
    if (!username) {
        return res.redirect('/login');
    }
    
    if (!stock_name || !stock_quantity) {
        return res.redirect('/profile?error=Missing+stock+information');
    }
    
    // Validate quantity
    const quantity = parseInt(stock_quantity, 10);
    if (isNaN(quantity) || quantity <= 0) {
        return res.redirect('/profile?error=Invalid+quantity');
    }
    
    // Insert into database
    const query = `
        INSERT INTO buy_orders (username, stock_name, stock_quantity, order_date)
        VALUES (?, ?, ?, datetime('now'))
    `;
    
    db.run(query, [username, stock_name, quantity], (err) => {
        if (err) {
            console.error('Database error:', err);
            return res.redirect(`/profile?error=Database+error:+${encodeURIComponent(err.message)}`);
        }
        
        // Redirect to profile page
        res.redirect('/profile?success=Order+placed+successfully');
    });
});

/**
 * GET /profile
 * Display user profile with buy orders
 */
app.get('/profile', (req, res) => {
    const username = req.session.username;
    
    if (!username) {
        return res.redirect('/login');
    }
    
    const query = `
        SELECT order_id, stock_name, stock_quantity, order_date 
        FROM buy_orders 
        WHERE username = ? 
        ORDER BY order_date DESC
    `;
    
    db.all(query, [username], (err, rows) => {
        if (err) {
            return res.status(500).send('Database error: ' + err.message);
        }
        
        const orders = rows || [];
        const error = req.query.error ? `<p style="color:red;">${req.query.error}</p>` : '';
        const success = req.query.success ? `<p style="color:green;">${req.query.success}</p>` : '';
        
        const html = `
            <!DOCTYPE html>
            <html>
            <head>
                <title>User Profile</title>
                <style>
                    table { border-collapse: collapse; margin: 20px 0; }
                    th, td { border: 1px solid black; padding: 10px; text-align: left; }
                    th { background-color: #f0f0f0; }
                </style>
            </head>
            <body>
                <h1>User Profile - ${username}</h1>
                ${error}${success}
                <h2>Buy Orders</h2>
                <table>
                    <tr>
                        <th>Order ID</th>
                        <th>Stock Name</th>
                        <th>Quantity</th>
                        <th>Order Date</th>
                    </tr>
                    ${orders.map(order => `
                        <tr>
                            <td>${order.order_id}</td>
                            <td>${order.stock_name}</td>
                            <td>${order.stock_quantity}</td>
                            <td>${order.order_date}</td>
                        </tr>
                    `).join('')}
                </table>
                <hr>
                <h3>Place New Order</h3>
                <form method="POST" action="/buy-stock">
                    <label for="stock_name">Stock Name:</label>
                    <input type="text" id="stock_name" name="stock_name" required>
                    <br><br>
                    <label for="stock_quantity">Quantity:</label>
                    <input type="number" id="stock_quantity" name="stock_quantity" min="1" required>
                    <br><br>
                    <button type="submit">Buy Stock</button>
                </form>
            </body>
            </html>
        `;
        
        res.send(html);
    });
});

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});