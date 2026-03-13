import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginService {

    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 1. Ensure inputs are present
        if (username == null || password == NULL) {
            return; // Return safely
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db", "u", "p")) {
            // 2. Query to check if username exists
            String sql = "SELECT password FROM users WHERE username = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        
                        // Check if password matches (Using plain comparison as per simple prompt, prefer hashing in prod)
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Correct, redirect to home page
                            response.sendRedirect("/home");
                            return;
                        }
                    } 
                    // If username doesn't exist or pass doesn't match:
                    // Return safely (implicitly doing nothing or handling response without redirect loop)
                }
            }
        } catch (SQLException e) {
            // Return safely
            e.printStackTrace();
        }
    }
}
