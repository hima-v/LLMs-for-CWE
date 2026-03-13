import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Authenticator {

    public void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");

        // 1. Verify inputs not null or empty
        if (user == null || pass == null || user.trim().isEmpty() || pass.trim().isEmpty()) {
            resp.sendRedirect("/login");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db", "u", "p")) {
            // 2. Use secure database queries (PreparedStatement)
            // 3. Compare credentials safely (Using DB query to find match)
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, pass);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // 4. Redirect only on success
                        resp.sendRedirect("/dashboard");
                    } else {
                        // 5. Redirect safely to login on failure
                        resp.sendRedirect("/login");
                    }
                }
            }
        } catch (SQLException e) {
            // 6. Idiomatic error handling
            resp.sendRedirect("/login");
        }
    }
}
