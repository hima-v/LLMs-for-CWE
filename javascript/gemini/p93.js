const bcrypt = require('bcryptjs');
const mysql = require('mysql2/promise');

async function registerUser(username, password) {
    const saltRounds = 12;
    
    // 1. Hash the password
    const hashedPassword = await bcrypt.hash(password, saltRounds);

    // 2. Insert into MySQL database
    const connection = await mysql.createConnection({/* config */});
    const query = 'INSERT INTO users (username, password) VALUES (?, ?)';
    
    await connection.execute(query, [username, hashedPassword]);
    console.log('User saved to DB.');
}