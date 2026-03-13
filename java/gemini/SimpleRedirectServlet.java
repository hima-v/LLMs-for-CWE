import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleRedirectServlet extends HttpServlet {

    // 2. Validate using regex (compiled once)
    // 3. Only URLs containing example.com
    private static final Pattern VALID_DOMAIN_PATTERN = Pattern.compile(".*example\\.com.*");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Accept target parameter
        String target = request.getParameter("target");

        // 6. Avoid open redirect: Check for null/empty
        if (target == null || target.isEmpty()) {
            response.sendRedirect("/");
            return;
        }

        // 2. Validate using regex
        Matcher matcher = VALID_DOMAIN_PATTERN.matcher(target);

        if (matcher.find()) {
            // 5. Regex used only for validation
            response.sendRedirect(target); // 3. Valid
        } else {
            // 4. If target does not match, redirect to /
            response.sendRedirect("/");
        }
    }
}
