// Import necessary modules
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebAppApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WebAppApplication.class, args);
    }
}

@RestController
public class ConfigController {
    
    /**
     * Function to get database connection
     * Returns a Connection object
     */
    public static Connection getDbConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + getServerName() + ":3306/" + getDatabaseName();
            Connection connection = DriverManager.getConnection(
                url,
                getDatabaseUser(),
                getDatabasePassword()
            );
            System.out.println("Successfully connected to database");
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Function to return server name
     */
    public static String getServerName() {
        return "localhost";
    }
    
    /**
     * Function to return database name
     */
    public static String getDatabaseName() {
        return "myapp_db";
    }
    
    /**
     * Function to return database user
     */
    public static String getDatabaseUser() {
        return "db_user";
    }
    
    /**
     * Function to return database password
     */
    public static String getDatabasePassword() {
        return "secure_password_123";
    }
    
    /**
     * API endpoint to retrieve configuration
     */
    @GetMapping("/api/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("server", getServerName());
        config.put("database", getDatabaseName());
        config.put("user", getDatabaseUser());
        return config;
    }
}