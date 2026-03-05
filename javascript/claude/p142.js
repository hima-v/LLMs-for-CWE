const express = require('express');
const mysql = require('mysql2/promise');
const app = express();

app.use(express.json());

const pool = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: 'password',
    database: 'subscribers_db'
});

async function unsubscribe(email) {
    let connection;
    try {
        connection = await pool.getConnection();
        
        // Check if email exists
        const [rows] = await connection.query(
            'SELECT id FROM subscribers WHERE email = ?',
            [email]
        );
        
        if (rows.length > 0) {
            // Delete the email
            await connection.query(
                'DELETE FROM subscribers WHERE email = ?',
                [email]
            );
            return {
                success: true,
                message: `Successfully unsubscribed ${email}`
            };
        } else {
            return {
                success: false,
                message: `The email ${email} is not in our database.`
            };
        }
    } catch (error) {
        return {
            success: false,
            message: `Database error: ${error.message}`
        };
    } finally {
        if (connection) connection.release();
    }
}

app.post('/unsubscribe', async (req, res) => {
    const { email } = req.body;
    
    if (!email || !email.trim()) {
        return res.status(400).json({
            success: false,
            message: 'Email address is required'
        });
    }
    
    const result = await unsubscribe(email.trim());
    return res.status(200).json(result);
});

app.listen(3000, () => console.log('Server running on port 3000'));