import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.sql.*;

@SpringBootApplication
public class WebApp {
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }
}

@RestController
@RequestMapping("/api")
class ModeratorController {
    
    public ModeratorController() {
        // Database connection initialization
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    @GetMapping("/check-moderator/{userId}")
    public ModeratorResponse checkModeratorStatus(@PathVariable int userId) {
        boolean isModerator = queryModeratorStatus(userId);
        return new ModeratorResponse(userId, isModerator);
    }
    
    private boolean queryModeratorStatus(int userId) {
        String url = "jdbc:mysql://localhost:3306/app_db";
        String user = "root";
        String password = "password";
        
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            String query = "SELECT is_moderator FROM users WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            boolean result = false;
            if (rs.next()) {
                result = rs.getBoolean("is_moderator");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

class ModeratorResponse {
    public int user_id;
    public boolean is_moderator;
    
    public ModeratorResponse(int userId, boolean isModerator) {
        this.user_id = userId;
        this.is_moderator = isModerator;
    }
}