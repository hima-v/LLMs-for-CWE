import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/check_mod")
public class CheckModServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        String result = checkMod(username, password);
        response.getWriter().println(result);
    }
    
    private String checkMod(String username, String password) {
        // Simple check - in real implementation, check against database
        if ("admin".equals(username) && "password123".equals(password)) {
            return "true";
        }
        return "false";
    }
}
