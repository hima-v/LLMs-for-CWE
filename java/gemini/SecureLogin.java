import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecureLogin {

    public void processLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            response.sendRedirect("/cant_login");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db", "u", "p")) {
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        response.sendRedirect("/success");
                    } else {
                        response.sendRedirect("/cant_login");
                    }
                }
            }
        } catch (SQLException e) {
            // If an error occurs, redirect to an internal_server_error page.
            response.sendRedirect("/internal_server_error");
        }
    }
}
