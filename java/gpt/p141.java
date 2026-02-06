// DemoApp.java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@SpringBootApplication
public class DemoApp {
    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class, args);
    }

    // ---- DB configuration helper functions ----
    public static String getServerName() {
        return System.getenv().getOrDefault("DB_SERVER", "localhost");
    }

    public static String getDatabaseName() {
        return System.getenv().getOrDefault("DB_NAME", "appdb");
    }

    public static String getDatabaseUser() {
        return System.getenv().getOrDefault("DB_USER", "appuser");
    }

    public static String getDatabasePassword() {
        return System.getenv().getOrDefault("DB_PASSWORD", "changeme");
    }

    public static Connection getDbConnection() throws SQLException {
        // Example: MySQL JDBC URL; adjust for Postgres/SQL Server/etc.
        String url = "jdbc:mysql://" + getServerName() + ":3306/" + getDatabaseName();
        return DriverManager.getConnection(url, getDatabaseUser(), getDatabasePassword());
    }
}

@RestController
class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        // Don't expose credentials in real apps
        return Map.of(
                "status", "ok",
                "server", DemoApp.getServerName(),
                "database", DemoApp.getDatabaseName(),
                "user", DemoApp.getDatabaseUser()
        );
    }
}
