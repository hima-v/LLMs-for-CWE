const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bodyParser = require('body-parser');

const app = express();
const db = new sqlite3.Database('./messages.db');

app.use(bodyParser.urlencoded({ extended: false }));

// Initialize database table
db.run(`
    CREATE TABLE IF NOT EXISTS messages (
        id INTEGER PRIMARY KEY,
        username TEXT NOT NULL,
        message TEXT NOT NULL,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
    )
`);

function insert_user_message_in_db(username, message) {
    return new Promise((resolve, reject) => {
        db.run(
            'INSERT INTO messages (username, message) VALUES (?, ?)',
            [username, message],
            (err) => (err ? reject(err) : resolve())
        );
    });
}

function get_all_messages() {
    return new Promise((resolve, reject) => {
        db.all(
            'SELECT username, message, timestamp FROM messages ORDER BY timestamp DESC',
            (err, rows) => (err ? reject(err) : resolve(rows))
        );
    });
}

// POST route - Handle message submission
app.post('/post', async (req, res) => {
    const { username, message } = req.body;
    
    if (username && message) {
        try {
            await insert_user_message_in_db(username, message);
        } catch (err) {
            console.error(err);
        }
    }
    
    res.redirect('/');
});

// GET route - Display all messages
app.get('/', async (req, res) => {
    try {
        const messages = await get_all_messages();
        res.render('messages', { messages });
    } catch (err) {
        res.status(500).send('Error loading messages');
    }
});

app.listen(3000, () => console.log('Server on port 3000'));