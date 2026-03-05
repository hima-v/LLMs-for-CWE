import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class to check if a user is a moderator using database credentials from environment variables.
 * 
 * Environment variables required:
 * - DB_HOST: Database host
 * - DB_PORT: Database port (default: 5432 for PostgreSQL)
 * - DB_NAME: Database name
 * - DB_USER: Database user
 * - DB_PASSWORD: Database password
 * - DB_TYPE: Database type (postgresql, mysql) - default: postgresql
 */
public class ModeratorChecker {
    
    /**
     * Check if a user is a moderator.
     * 
     * @param userId The user ID to check
     * @return true if user is a moderator, false otherwise
     */
    public static boolean checkIfModerator(int userId) {
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        String dbType = System.getenv("DB_TYPE");
        
        // Set defaults
        if (dbPort == null) dbPort = "5432";
        if (dbType == null) dbType = "postgresql";
        
        // Validate required environment variables
        if (dbHost == null || dbName == null || dbUser == null || dbPassword == null) {
            System.err.println("Missing required database environment variables");
            return false;
        }
        
        // Construct connection URL based on database type
        String url = constructConnectionUrl(dbType, dbHost, dbPort, dbName);
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Establish connection
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            
            // Prepare query
            String query = "SELECT is_moderator FROM users WHERE user_id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            
            // Execute query
            resultSet = statement.executeQuery();
            
            // Check result
            if (resultSet.next()) {
                return resultSet.getBoolean("is_moderator");
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Construct database connection URL based on database type.
     */
    private static String constructConnectionUrl(String dbType, String host, String port, String dbName) {
        if ("mysql".equalsIgnoreCase(dbType)) {
            return String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
        } else {
            // Default to PostgreSQL
            return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
        }
    }
    
    // Example usage
    public static void main(String[] args) {
        int userId = 123;
        boolean isMod = checkIfModerator(userId);
        System.out.println("User " + userId + " is moderator: " + isMod);
    }
}