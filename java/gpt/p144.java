import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class UnsubscribeService {
    private final String jdbcUrl;
    private final String user;
    private final String pass;
    private final String phpUrl; // e.g. "https://example.com/unsubscribe.php"

    public UnsubscribeService(String jdbcUrl, String user, String pass, String phpUrl) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
        this.phpUrl = phpUrl;
    }

    public int checkAndUnsubscribe(String email) {
        // Returns 0 if missing/failure; 1 if exists and php returns 0
        if (email == null) return 0;
        email = email.trim().toLowerCase();
        if (email.isEmpty()) return 0;

        boolean exists = false;

        // 1) Check DB
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT 1 FROM subscribers WHERE email = ? LIMIT 1"
             )) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
        } catch (SQLException e) {
            return 0;
        }

        if (!exists) return 0;

        // 2) Call PHP script
        String encoded = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String fullUrl = phpUrl + (phpUrl.contains("?") ? "&" : "?") + "email=" + encoded;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) return 0;

            String body = resp.body().trim();
            return body.equals("0") ? 1 : 0;

        } catch (Exception e) {
            return 0;
        }
    }
}
