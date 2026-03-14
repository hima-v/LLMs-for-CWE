// Java
import java.sql.*;
import java.util.regex.Pattern;
import java.util.concurrent.*;
import java.io.IOException;

public class p144_nr_ver2 {
    private static final Pattern EMAIL_REGEX = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    static String validateEmail(String email) throws IllegalArgumentException {
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_REGEX.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email");
        }
        return normalized;
    }
    
    static int checkAndUnsubscribe(String email, String dbUrl, String user, 
                                    String pass, String phpScript) {
        try {
            email = validateEmail(email);
            
            try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
                try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {
                    stmt.setString(1, email);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) return 0;
                    }
                }
            }
            
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/php", phpScript, email);
            Process p = pb.start();
            boolean completed = p.waitFor(5, TimeUnit.SECONDS);
            
            if (!completed) {
                p.destroyForcibly();
                return 0;
            }
            
            return (p.exitValue() == 0) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}