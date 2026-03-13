import java.sql.*;
import javax.servlet.http.*;

public class LoginCheck {
    public void login(HttpServletRequest req, HttpServletResponse resp) {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        // 1. Validate inputs
        if (u == null || p == null) return;

        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test")) {
            // 2. Safe parameterized query
            try (PreparedStatement ps = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
                ps.setString(1, u);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString(1);
                        // 3. Compare securely
                        if (stored != null && stored.equals(p)) {
                            // 4. Success
                            resp.sendRedirect("/home");
                        }
                        // 5. Return safely on mismatch
                    }
                }
            }
        } catch (Exception e) {
            // 5. Return safely on error
        }
    }
}
