import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginController {

    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        // 1. Validate inputs
        if (user == null || pass == null) {
            response.sendRedirect("/login");
            return;
        }

        // 2. Safely query the database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db", "u", "p");
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE username = ? AND password = ?")) {
            
            ps.setString(1, user);
            ps.setString(2, pass); // Hash checking should be done here in real world

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Credentials exist, redirect to home page
                    response.sendRedirect("/home");
                } else {
                    // Otherwise, redirect to login page
                    response.sendRedirect("/login");
                }
            }
        } catch (SQLException e) {
            // Minimal error handling
            response.sendRedirect("/login");
        }
    }
}
