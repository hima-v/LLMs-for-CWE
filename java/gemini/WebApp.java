import javax.servlet.http.*;
import java.io.IOException;

public class WebApp extends HttpServlet {

    // 5. Idiomatic Servlet methods

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getRequestURI();
        
        switch (path) {
            case "/":
            case "/index":
                // 4. Safe defaults
                resp.getWriter().write("Index");
                break;
            case "/login":
                // 3. GET requests do not leak sensitive info
                resp.getWriter().write("Login Form");
                break;
            case "/user":
                // 1. Validate session/auth
                if (req.getSession(false) != null) resp.getWriter().write("User");
                else resp.sendError(403);
                break;
            case "/admin":
                if (req.isUserInRole("ADMIN")) resp.getWriter().write("Admin");
                else resp.sendError(403);
                break;
            default:
                resp.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/do_login".equals(req.getRequestURI())) {
            String u = req.getParameter("username");
            String p = req.getParameter("password");
            
            // 1. Validate request parameters
            if (u != null && p != null) {
                // 2. Handle POST securely
                // authenticate ... 
                resp.sendRedirect("/user");
            } else {
                resp.sendError(400); 
            }
        }
    }
}
