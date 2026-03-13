import java.sql.*;
import javax.servlet.http.*;

public class LoginFunc {
    public void doLogin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        // 1. Validate inputs
        if (u == null || p == null || u.isEmpty()) {
            resp.sendRedirect("/login");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://db", "u", "p")) {
            // 2. Safe parameterized query
            String sql = "SELECT id FROM users WHERE username=? AND password=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u);
                ps.setString(2, p);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // 4. Success -> Home
                        resp.sendRedirect("/home");
                    } else {
                        // 5. Failure -> Login
                        resp.sendRedirect("/login");
                    }
                }
            }
        } catch (Exception e) {
            resp.sendRedirect("/login");
        }
    }
}
