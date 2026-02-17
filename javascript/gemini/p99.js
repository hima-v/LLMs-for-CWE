const bcrypt = require('bcryptjs');
const mysql = require('mysql2');

async function registerUser(username, password) {
    const saltRounds = 10;
    
    // 1. Hash the password
    const hashedPassword = await bcrypt.hash(password, saltRounds);

    // 2. Insert into Database
    const connection = mysql.createConnection({ /* config */ });
    const query = 'INSERT INTO users (username, password) VALUES (?, ?)';
    
    connection.execute(query, [username, hashedPassword], (err, results) => {
        if (err) throw err;
        console.log('User registered successfully');
    });
}