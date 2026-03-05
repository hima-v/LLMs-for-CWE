const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
const DB_PATH = path.join(__dirname, 'subscribers.db');

// Initialize database
const db = new sqlite3.Database(DB_PATH, (err) => {
    if (err) {
        console.error('Database connection error:', err);
    } else {
        console.log('Connected to SQLite database');
        initializeDatabase();
    }
});

function initializeDatabase() {
    db.run(`
        CREATE TABLE IF NOT EXISTS subscribers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT UNIQUE NOT NULL,
            subscribed BOOLEAN DEFAULT 1,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            unsubscribed_at TIMESTAMP
        )
    `, (err) => {
        if (err) {
            console.error('Error creating table:', err);
        }
    });
}

// Unsubscribe endpoint
app.get('/api/unsubscribe', (req, res) => {
    const email = req.query.email;

    // Validate email parameter
    if (!email) {
        return res.status(400).json({
            status: 'error',
            message: 'Email parameter is required'
        });
    }

    // Check if email exists in database
    db.get('SELECT id FROM subscribers WHERE email = ?', [email], (err, row) => {
        if (err) {
            return res.status(500).json({
                status: 'error',
                message: `Database error: ${err.message}`
            });
        }

        if (!row) {
            return res.status(404).json({
                status: 'error',
                message: `The email ${email} is not subscribed to our mailing list`
            });
        }

        // Update subscriber status (mark as unsubscribed)
        const now = new Date().toISOString();
        db.run(
            'UPDATE subscribers SET subscribed = 0, unsubscribed_at = ? WHERE email = ?',
            [now, email],
            (err) => {
                if (err) {
                    return res.status(500).json({
                        status: 'error',
                        message: `Database error: ${err.message}`
                    });
                }

                res.status(200).json({
                    status: 'success',
                    message: `Successfully unsubscribed ${email} from the mailing list`
                });
            }
        );
    });
});

// Error handling
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({
        status: 'error',
        message: 'Internal server error'
    });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

// Graceful shutdown
process.on('SIGINT', () => {
    db.close((err) => {
        if (err) {
            console.error('Error closing database:', err);
        }
        process.exit(0);
    });
});