import java.sql.*;
import java.util.regex.Pattern;

public class p144_nre {
    public int manageUnsubscribe(String email) {
        if (!Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", email)) return 0;
        
        String url = "jdbc:sqlite:users.db";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM subscribers WHERE email = ?")) {
            
            pstmt.setString(1, email);
            if (!pstmt.executeQuery().next()) return 0;

            ProcessBuilder pb = new ProcessBuilder("/usr/bin/php", "/opt/app/unsub.php", email);
            Process p = pb.start();
            if (p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                return (p.getInputStream().readAllBytes().length > 0 && 
                        new String(p.getInputStream().readAllBytes()).trim().equals("0")) ? 1 : 0;
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}