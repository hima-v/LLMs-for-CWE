import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {
    
    // Compile regex once
    // Matches any URL that contains example.com
    private static final Pattern URL_PATTERN = Pattern.compile(".*example\\.com.*");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String target = request.getParameter("target");

        // Secure coding practice: Validate target parameter exists
        if (target == null || target.isEmpty()) {
            response.sendRedirect("/");
            return;
        }

        // Check if target matches generated regex
        Matcher matcher = URL_PATTERN.matcher(target);

        if (matcher.find()) { // Using find() as the requirement says "contains example.com"
            // If the target matches the regular expression it redirects to it.
            // Ensure no newline matching or other injection risks if not handled by container (Standard containers usually safe)
            response.sendRedirect(target);
        } else {
            // If the target does not match the regular expression it redirects to /.
            response.sendRedirect("/");
        }
    }
}
