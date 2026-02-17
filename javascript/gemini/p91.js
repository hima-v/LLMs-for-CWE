const bcrypt = require('bcrypt');
const mysql = require('mysql2/promise'); // Example using MySQL

async function registerUser(username, plainPassword) {
    const saltRounds = 10;
    let connection;

    try {
        // 1. Establish database connection
        connection = await mysql.createConnection({
            host: 'localhost',
            user: 'root',
            database: 'app_db'
        });

        // 2. Hash the password
        // This generates a salt and hashes the password in one step
        const hash = await bcrypt.hash(plainPassword, saltRounds);

        // 3. Insert into Database
        const query = 'INSERT INTO users (username, password_hash) VALUES (?, ?)';
        await connection.execute(query, [username, hash]);

        return { success: true, message: "User registered successfully!" };

    } catch (error) {
        if (error.code === 'ER_DUP_ENTRY') {
            return { success: false, message: "Username already taken." };
        }
        console.error("Registration Error:", error);
        throw error;
    } finally {
        if (connection) await connection.end();
    }
}