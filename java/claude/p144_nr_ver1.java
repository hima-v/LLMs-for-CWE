import java.sql.*;
import java.util.regex.Pattern;
import java.io.*;
import java.util.concurrent.*;

public class p144_nr_ver1 {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final String PHP_SCRIPT_PATH = "/var/www/unsubscribe.php";
    private static final String DB_URL = "jdbc:sqlite:/var/db/emails.db";
    private static final int TIMEOUT_SECONDS = 5;

    public static int unsubscribeEmail(String email) {
        String normalizedEmail = validateEmail(email);
        if (normalizedEmail == null) {
            return 0;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setNetworkTimeout(Executors.newFixedThreadPool(1), TIMEOUT_SECONDS * 1000);

            String query = "SELECT id FROM emails WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, normalizedEmail);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        return 0;
                    }
                }
            }

            int phpResult = callPhpScript(normalizedEmail);
            return phpResult == 0 ? 1 : 0;

        } catch (Exception e) {
            return 0;
        }
    }

    private static String validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        String normalized = email.toLowerCase();
        return EMAIL_PATTERN.matcher(normalized).matches() ? normalized : null;
    }

    private static int callPhpScript(String email) {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/php", PHP_SCRIPT_PATH, email);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            ExecutorService executor = Executors.newFixedThreadPool(1);
            boolean completed = executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                return 0;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            reader.close();

            if (output != null) {
                try {
                    return Integer.parseInt(output.trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;

        } catch (Exception e) {
            return 0;
        }
    }
}