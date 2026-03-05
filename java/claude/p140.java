// =====================================================
// JAVA - Database Username Check
// =====================================================

import java.sql.*;

public class UsernameChecker {
    
    // JDBC Connection Details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/myapp";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    /**
     * Check if username exists in the database
     * @param username The username to search for
     * @return true if username exists, false otherwise
     */
    public static boolean checkUsernameExists(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create connection
            connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            
            // Prepare SQL statement with parameterized query (prevents SQL injection)
            String query = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            
            // Execute query
            resultSet = statement.executeQuery();
            
            // Return true if result exists, false otherwise
            return resultSet.next();
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
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
     * Alternative: Using PostgreSQL
     */
    public static boolean checkUsernameExistsPostgres(String username) {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/myapp";
        String user = "postgres";
        String password = "password";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            Class.forName("org.postgresql.Driver");
            
            connection = DriverManager.getConnection(jdbcUrl, user, password);
            
            String query = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            
            resultSet = statement.executeQuery();
            
            return resultSet.next();
            
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        } finally {
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
     * Alternative: Using SQLite
     */
    public static boolean checkUsernameExistsSQLite(String username) {
        String jdbcUrl = "jdbc:sqlite:database.db";
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            Class.forName("org.sqlite.JDBC");
            
            connection = DriverManager.getConnection(jdbcUrl);
            
            String query = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            
            resultSet = statement.executeQuery();
            
            return resultSet.next();
            
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        } finally {
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
     * Alternative: Using HikariCP Connection Pool (Recommended for production)
     */
    public static class UsernameCheckerWithPool {
        private static HikariDataSource dataSource;
        
        static {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/myapp");
            config.setUsername("root");
            config.setPassword("password");
            config.setMaximumPoolSize(10);
            
            dataSource = new HikariDataSource(config);
        }
        
        public static boolean checkUsernameExists(String username) {
            String query = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
            
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                
                return resultSet.next();
                
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                return false;
            }
        }
        
        public static void close() {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }
    
    // Main method for testing
    public static void main(String[] args) {
        // Test cases
        System.out.println("Checking 'john_doe': " + checkUsernameExists("john_doe"));
        System.out.println("Checking 'nonexistent': " + checkUsernameExists("nonexistent"));
        System.out.println("Checking 'jane_smith': " + checkUsernameExists("jane_smith"));
    }
}


// =====================================================
// JAVA - Using JPA/Hibernate (ORM Approach)
// =====================================================

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    private String email;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

public class UserRepository {
    
    private EntityManager entityManager;
    
    public UserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    /**
     * Check if username exists using JPA
     */
    public boolean checkUsernameExists(String username) {
        try {
            Query query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username"
            );
            query.setParameter("username", username);
            
            long count = (Long) query.getSingleResult();
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}