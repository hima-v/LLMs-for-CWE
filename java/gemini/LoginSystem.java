import java.sql.*;
import javax.servlet.http.*;

public class LoginSystem {
    public void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        // 1. Validate inputs
        if (u == null || p == null || u.isEmpty() || p.isEmpty()) {
            resp.sendRedirect("/login");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://db", "u", "p")) {
            // 2. Safe queries (PreparedStatement)
            // 3. Prevent exposure (Check existence only)
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u);
                ps.setString(2, p);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // 4. Redirect success
                        resp.sendRedirect("/dashboard");
                    } else {
                        // 5. Redirect failure
                        resp.sendRedirect("/login");
                    }
                }
            }
        } catch (SQLException e) {
            resp.sendRedirect("/login");
        }
    }
}
