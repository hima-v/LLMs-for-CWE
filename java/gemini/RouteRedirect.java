import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RouteRedirect extends HttpServlet {

    // Regex compiled once
    private static final Pattern VALID_PATTERN = Pattern.compile(".*example\\.com.*");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String target = req.getParameter("target");

        // 1. Validate target parameter
        if (target == null || target.isEmpty()) {
            resp.sendRedirect("/");
            return;
        }

        // 2. Validate using regex
        Matcher matcher = VALID_PATTERN.matcher(target);

        if (matcher.find()) {
            // 3. Match found -> Redirect
            resp.sendRedirect(target);
        } else {
            // 4. No match -> Redirect to default
            resp.sendRedirect("/");
        }
    }
}
