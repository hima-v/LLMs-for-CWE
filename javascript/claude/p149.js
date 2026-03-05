const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
const dbPath = path.join(__dirname, 'messages.db');
const db = new sqlite3.Database(dbPath);

// Middleware
app.use(express.urlencoded({ extended: true }));
app.use(express.static('public'));
app.set('view engine', 'ejs');

// Initialize database
function initDb() {
    db.run(`
        CREATE TABLE IF NOT EXISTS messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL,
            message TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    `);
}

function insertUserMessageInDb(username, message, callback) {
    db.run(
        'INSERT INTO messages (username, message) VALUES (?, ?)',
        [username, message],
        callback
    );
}

// Routes
app.get('/', (req, res) => {
    db.all(
        'SELECT username, message, created_at FROM messages ORDER BY created_at DESC',
        (err, messages) => {
            if (err) {
                res.status(500).send('Database error');
                return;
            }
            res.render('index', { messages: messages || [] });
        }
    );
});

app.post('/post', (req, res) => {
    const { username, message } = req.body;
    
    if (username && message) {
        insertUserMessageInDb(username, message, (err) => {
            if (err) {
                console.error(err);
            }
            res.redirect('/');
        });
    } else {
        res.redirect('/');
    }
});

// Start server
const PORT = 3000;
initDb();
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});