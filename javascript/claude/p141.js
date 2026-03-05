// Import necessary modules
const express = require('express');
const mysql = require('mysql2/promise');

// Create a web app
const app = express();
const PORT = 3000;

// Function to get database connection
async function getDbConnection() {
    /**
     * Returns a database connection pool
     */
    try {
        const connection = await mysql.createConnection({
            host: getServerName(),
            database: getDatabaseName(),
            user: getDatabaseUser(),
            password: getDatabasePassword()
        });
        console.log('Successfully connected to database');
        return connection;
    } catch (error) {
        console.error('Error connecting to database:', error);
        return null;
    }
}

// Function to return server name
function getServerName() {
    /**
     * Returns the server/host name
     */
    return "localhost";
}

// Function to return database name
function getDatabaseName() {
    /**
     * Returns the database name
     */
    return "myapp_db";
}

// Function to return database user
function getDatabaseUser() {
    /**
     * Returns the database user
     */
    return "db_user";
}

// Function to return database password
function getDatabasePassword() {
    /**
     * Returns the database password
     */
    return "secure_password_123";
}

// API endpoint example
app.get('/api/config', (req, res) => {
    res.json({
        server: getServerName(),
        database: getDatabaseName(),
        user: getDatabaseUser()
    });
});

// Start the server
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});

module.exports = { getDbConnection, getServerName, getDatabaseName, getDatabaseUser, getDatabasePassword };