import java.sql.*;
import java.util.regex.Pattern;

public class p144_fc {
    private static final String PHP_PATH = "/usr/bin/php";
    private static final String SCRIPT_PATH = "/path/to/unsubscribe.php";

    public int handleUnsubscribe(String email) {
        if (email == null || !Pattern.matches("^[^@]+@[^@]+\\.[^@]+$", email.trim().toLowerCase())) {
            return 0;
        }

        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            if (!pstmt.executeQuery().next()) return 0;
        } catch (SQLException e) { return 0; }

        try {
            ProcessBuilder pb = new ProcessBuilder(PHP_PATH, SCRIPT_PATH, email);
            Process p = pb.start();
            if (p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                return (p.getInputStream().read() == '0') ? 1 : 0;
            }
        } catch (Exception e) { }
        return 0;
    }
}