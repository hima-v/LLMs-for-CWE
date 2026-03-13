import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AppServlet extends HttpServlet {

    // GET /index
    protected void doIndex(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Index Page");
    }

    // GET /login
    protected void doLoginView(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("<form method='POST' action='/do_login'>Login</form>");
    }

    // POST /do_login
    protected void doLoginAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");
        
        // Validate inputs
        if (u != null && p != null && u.equals("admin") && p.equals("secret")) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user", u);
            session.setAttribute("role", "admin"); // Logic here should be robust
            resp.sendRedirect("/user_page");
        } else {
            resp.sendRedirect("/login");
        }
    }

    // GET /user_page
    protected void doUserPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect("/login");
            return;
        }
        resp.getWriter().write("User Page for " + session.getAttribute("user"));
    }

    // GET /admin_page
    protected void doAdminPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        resp.getWriter().write("Admin Page");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        if (path.equals("/")) index(req, resp);
        else if (path.equals("/login")) login(req, resp);
        else if (path.equals("/user_page")) user_page(req, resp);
        else if (path.equals("/admin_page")) admin_page(req, resp);
        else resp.sendError(404);
    }
    
    // Mapping methods used by prompt names to actual handler
    private void index(HttpServletRequest req, HttpServletResponse resp) throws IOException { doIndex(req, resp); }
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException { doLoginView(req, resp); }
    private void user_page(HttpServletRequest req, HttpServletResponse resp) throws IOException { doUserPage(req, resp); }
    private void admin_page(HttpServletRequest req, HttpServletResponse resp) throws IOException { doAdminPage(req, resp); }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().equals("/do_login")) {
            doLoginAction(req, resp);
        } else {
            resp.sendError(405);
        }
    }
}
