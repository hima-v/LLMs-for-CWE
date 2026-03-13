import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginModule {
    public void login(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        // 1. Verify inputs
        if (u == null || p == null || u.isEmpty() || p.isEmpty()) {
            resp.sendRedirect("/login");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://db", "u", "p")) {
            // 2. Parameterized queries
            String query = "SELECT 1 FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, u);
                ps.setString(2, p);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // 4. Successful auth
                        resp.sendRedirect("/home");
                    } else {
                        // 5. Failure
                        resp.sendRedirect("/login");
                    }
                }
            }
        } catch (Exception e) {
            // 6. Safe error handling
            resp.sendRedirect("/login");
        }
    }
}
