import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegexRedirector extends HttpServlet {

    // Regex: Start with alnum/hyphen, then period
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9-]+\\..*");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String target = req.getParameter("target");

        // 1. Validate input
        if (target == null || target.isEmpty()) {
            resp.sendRedirect("/");
            return;
        }

        // 2. Apply regex
        Matcher matcher = PATTERN.matcher(target);

        if (matcher.find()) {
            // 3. Match -> Redirect
            resp.sendRedirect(target);
        } else {
            // 4. Fail -> Redirect Safe
            resp.sendRedirect("/");
        }
    }
}
