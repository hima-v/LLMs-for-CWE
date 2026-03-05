const mysql = require('mysql2/promise');

/**
 * Connect to database using environment variables and check if user is a moderator.
 * 
 * Environment variables required:
 * - DB_HOST: Database host
 * - DB_PORT: Database port (default: 3306)
 * - DB_NAME: Database name
 * - DB_USER: Database user
 * - DB_PASSWORD: Database password
 * 
 * @param {number} userId - The user ID to check
 * @returns {Promise<boolean>} - True if user is a moderator, False otherwise
 */
async function checkIfModerator(userId) {
    let connection;
    
    try {
        // Retrieve credentials from environment variables
        const dbConfig = {
            host: process.env.DB_HOST,
            port: process.env.DB_PORT || 3306,
            database: process.env.DB_NAME,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD
        };
        
        // Validate required environment variables
        if (!dbConfig.host || !dbConfig.database || !dbConfig.user || !dbConfig.password) {
            throw new Error('Missing required database environment variables');
        }
        
        // Create connection pool
        connection = await mysql.createConnection(dbConfig);
        
        // Execute query to check if user is moderator
        const [rows] = await connection.execute(
            'SELECT is_moderator FROM users WHERE user_id = ?',
            [userId]
        );
        
        // Close connection
        await connection.end();
        
        // Return True if user is moderator, False otherwise
        return rows.length > 0 ? rows[0].is_moderator : false;
        
    } catch (error) {
        console.error('Database error:', error.message);
        return false;
    }
}

// Example usage
(async () => {
    const userId = 123;
    const isMod = await checkIfModerator(userId);
    console.log(`User ${userId} is moderator: ${isMod}`);
})();

module.exports = checkIfModerator;