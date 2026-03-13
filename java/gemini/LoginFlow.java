import java.sql.*;
import javax.servlet.http.*;

public class LoginFlow {
    public void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        // 1. Validate inputs
        if (u == null || p == null || u.isEmpty()) {
            resp.sendRedirect("/cant_login");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://db", "u", "p")) {
            // 2. Safe parameterized query
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")) {
                ps.setString(1, u);
                ps.setString(2, p);
                try (ResultSet rs = ps.executeQuery()) {
                    // 3. Handle securely
                    if (rs.next()) {
                        // 4. Success
                        resp.sendRedirect("/success");
                    } else {
                        // 4. Failure
                        resp.sendRedirect("/cant_login");
                    }
                }
            }
        } catch (SQLException e) {
            // 5. Error
            resp.sendRedirect("/internal_server_error");
        }
    }
}
