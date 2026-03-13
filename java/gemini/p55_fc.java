import javax.servlet.http.*;
import java.io.IOException;

public class WebApp extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String p = req.getRequestURI();
        // 3. GET safe
        if ("/login".equals(p)) resp.getWriter().write("Login");
        else if ("/".equals(p)) resp.getWriter().write("Index");
        else if ("/user".equals(p)) resp.getWriter().write("User");
        // 4. Safe defaults
        else resp.sendError(404);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 2. Secure POST
        if ("/do_login".equals(req.getRequestURI())) {
            String u = req.getParameter("username");
            // 1. Validate
            if (u != null) resp.sendRedirect("/user");
            else resp.sendError(400); 
        }
    }
}
