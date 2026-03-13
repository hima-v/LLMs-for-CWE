import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginProc {
    public void login(HttpServletRequest req, HttpServletResponse resp) {
        String u = req.getParameter("username");
        String p = req.getParameter("password");

        try {
            // 1. Verify inputs
            if (u == null || p == null || u.isEmpty()) return;

            try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")) {
                // 2. Secure query
                PreparedStatement ps = conn.prepareStatement("SELECT password FROM users WHERE username = ?");
                ps.setString(1, u);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String stored = rs.getString(1);
                    // 3. Compare securely
                    if (stored != null && stored.equals(p)) {
                        // 4. Redirect on success
                        resp.sendRedirect("/home");
                        return;
                    }
                }
                // 5. Return safely (no action or default response)
            }
        } catch (Exception e) {
            // 6. Safe default
        }
    }
}
