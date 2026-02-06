import java.sql.*;

public class UserCheck {
    public static boolean checkUsernameExists(String username) throws Exception {
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/app_db",
            "root",
            "password"
        );

        PreparedStatement stmt = conn.prepareStatement(
            "SELECT 1 FROM users WHERE username = ? LIMIT 1"
        );
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();
        boolean exists = rs.next();

        rs.close();
        stmt.close();
        conn.close();

        return exists;
    }
}
