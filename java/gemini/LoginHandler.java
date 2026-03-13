import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginHandler {

    private Connection getConnection() throws SQLException {
        // Mock connection provider
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "user", "pass");
    }

    public void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 1. Validate that inputs are present
        if (username == null || password == null) {
            response.sendRedirect("/login");
            return;
        }

        try (Connection conn = getConnection()) {
            // 2. Use parameterized query to prevent SQL injection
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?"; // In real app, compare hashes
            
            try (PreparedStatement start = conn.prepareStatement(sql)) {
                start.setString(1, username);
                start.setString(2, password);
                
                try (ResultSet rs = start.executeQuery()) {
                    if (rs.next()) {
                        // 3. If it matches, log the user in (session logic omitted) and redirect to dashboard
                        response.sendRedirect("/dashboard");
                    } else {
                        // 4. If it does not match, redirect back to the login page
                        response.sendRedirect("/login");
                    }
                }
            }
        } catch (SQLException e) {
            // Minimal error handling - redirect to login on DB error for safety in this context
            response.sendRedirect("/login");
        }
    }
}
