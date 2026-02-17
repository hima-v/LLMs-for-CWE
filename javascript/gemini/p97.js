const bcrypt = require('bcryptjs');
const mysql = require('mysql2/promise');

async function registerUser(username, password, firstName, lastName, email) {
    const connection = await mysql.createConnection({/* config */});

    // Check if username exists
    const [rows] = await connection.execute('SELECT * FROM users WHERE username = ?', [username]);
    
    if (rows.length > 0) {
        return "Username already exists.";
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    // Insert user
    await connection.execute(
        'INSERT INTO users (username, password_hash, first_name, last_name, email) VALUES (?, ?, ?, ?, ?)',
        [username, hashedPassword, firstName, lastName, email]
    );

    return "Registration succeeded.";
}