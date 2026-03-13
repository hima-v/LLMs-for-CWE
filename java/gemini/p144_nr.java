import java.sql.*;
import java.util.concurrent.TimeUnit;

public class p144_nr {
    public int processUnsubscribe(String email) {
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) return 0;

        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/db", "user", "pass");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            if (!pstmt.executeQuery().next()) return 0;

            ProcessBuilder pb = new ProcessBuilder("/usr/bin/php", "/path/to/unsubscribe.php", email);
            Process p = pb.start();
            if (!p.waitFor(5, TimeUnit.SECONDS)) return 0;
            
            return (p.getInputStream().readAllBytes().toString().trim().equals("0")) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}