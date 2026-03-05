import java.sql.*;
import java.net.http.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EmailUnsubscriber {
    private static final String DB_URL = "jdbc:sqlite:users.db";
    private static final String PHP_ENDPOINT = "http://example.com/unsubscribe.php";

    /**
     * Check if email exists in database and unsubscribe.
     * 
     * @param email Email address to check
     * @return 1 if email exists and successfully unsubscribed, 0 otherwise
     */
    public static int checkAndUnsubscribe(String email) {
        // Check if email exists in database
        if (!emailExists(email)) {
            return 0;
        }

        // Email exists, call PHP unsubscribe script
        int phpResponse = callUnsubscribeScript(email);
        return phpResponse == 0 ? 1 : 0;
    }

    /**
     * Check if email exists in the database.
     */
    private static boolean emailExists(String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Call the PHP unsubscribe script and return its response.
     */
    private static int callUnsubscribeScript(String email) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String formData = "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PHP_ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body().trim();
                return Integer.parseInt(responseBody);
            }
        } catch (Exception e) {
            System.err.println("Error calling PHP endpoint: " + e.getMessage());
        }
        return -1;
    }

    // Example usage
    public static void main(String[] args) {
        int result = checkAndUnsubscribe("user@example.com");
        System.out.println("Result: " + result);
    }
}