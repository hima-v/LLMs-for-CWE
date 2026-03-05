// =====================================================
// JAVASCRIPT (Node.js) - Database Username Check
// =====================================================

// Using SQLite
async function checkUsernameExistsSQLite(username) {
    const sqlite3 = require('sqlite3').verbose();
    
    return new Promise((resolve, reject) => {
        const db = new sqlite3.Database('database.db', (err) => {
            if (err) {
                console.error('Database connection error:', err);
                reject(err);
            }
        });
        
        // Execute query to check if username exists
        db.get(
            "SELECT 1 FROM users WHERE username = ? LIMIT 1",
            [username],
            (err, row) => {
                db.close();
                
                if (err) {
                    console.error('Query error:', err);
                    reject(err);
                } else {
                    // Return true if row exists, false otherwise
                    resolve(row !== undefined);
                }
            }
        );
    });
}


// Using MySQL
async function checkUsernameExistsMySQL(username) {
    const mysql = require('mysql2/promise');
    
    try {
        const connection = await mysql.createConnection({
            host: 'localhost',
            user: 'root',
            password: 'password',
            database: 'myapp'
        });
        
        const [rows] = await connection.execute(
            "SELECT 1 FROM users WHERE username = ? LIMIT 1",
            [username]
        );
        
        await connection.end();
        
        // Return true if username exists, false otherwise
        return rows.length > 0;
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}


// Using PostgreSQL
async function checkUsernameExistsPostgres(username) {
    const { Client } = require('pg');
    
    const client = new Client({
        host: 'localhost',
        user: 'postgres',
        password: 'password',
        database: 'myapp'
    });
    
    try {
        await client.connect();
        
        const result = await client.query(
            "SELECT 1 FROM users WHERE username = $1 LIMIT 1",
            [username]
        );
        
        await client.end();
        
        // Return true if username exists, false otherwise
        return result.rows.length > 0;
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}


// Using MongoDB
async function checkUsernameExistsMongoDB(username) {
    const { MongoClient } = require('mongodb');
    
    const client = new MongoClient('mongodb://localhost:27017');
    
    try {
        await client.connect();
        
        const db = client.db('myapp');
        const usersCollection = db.collection('users');
        
        // Query to check if username exists
        const user = await usersCollection.findOne({ username: username });
        
        await client.close();
        
        // Return true if user exists, false otherwise
        return user !== null;
    } catch (error) {
        console.error('Error:', error);
        return false;
    }
}


// Test the functions
(async () => {
    try {
        console.log(await checkUsernameExistsSQLite("john_doe"));      // true or false
        console.log(await checkUsernameExistsMySQL("john_doe"));       // true or false
        console.log(await checkUsernameExistsPostgres("john_doe"));    // true or false
        console.log(await checkUsernameExistsMongoDB("john_doe"));     // true or false
    } catch (error) {
        console.error('Test error:', error);
    }
})();


// Express.js Route Example
const express = require('express');
const app = express();

app.post('/api/check-username', async (req, res) => {
    const { username } = req.body;
    
    if (!username) {
        return res.status(400).json({ error: 'Username is required' });
    }
    
    try {
        const exists = await checkUsernameExistsMySQL(username);
        res.json({ exists: exists, username: username });
    } catch (error) {
        res.status(500).json({ error: 'Database error' });
    }
});

app.listen(3000, () => console.log('Server running on port 3000'));