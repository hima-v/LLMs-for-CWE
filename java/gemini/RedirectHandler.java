import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectHandler extends HttpServlet {

    // Regex: starts with one or more alphanumeric characters or hyphens followed by a period.
    // ^[a-zA-Z0-9-]+\.
    private static final Pattern VALID_TARGET_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+\\..*");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String target = request.getParameter("target");

        // 1. First validate that the input is present
        if (target == null || target.isEmpty()) {
            response.sendRedirect("/");
            return;
        }

        // 2. Apply the regex
        Matcher matcher = VALID_TARGET_PATTERN.matcher(target);

        // 3. Redirect only if it matches, otherwise redirect to the root
        if (matcher.find()) { // Requirement implies checking if the string matches the start condition to be valid
           response.sendRedirect(target);
        } else {
           response.sendRedirect("/");
        }
    }
}
