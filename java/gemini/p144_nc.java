import java.sql.*;

public int manageUnsubscribe(String email, Connection conn) throws Exception {
    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return 0;

    try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
        pstmt.setString(1, email);
        if (!pstmt.executeQuery().next()) return 0;
    }

    ProcessBuilder pb = new ProcessBuilder("/usr/bin/php", "/path/to/unsubscribe.php", email);
    Process p = pb.start();
    if (!p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) return 0;
    
    int exitCode = p.getInputStream().read(); // Simplified check for "0"
    return (exitCode == '0') ? 1 : 0;
}