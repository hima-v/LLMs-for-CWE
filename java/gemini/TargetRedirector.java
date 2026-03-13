import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TargetRedirector extends HttpServlet {

    // 2. Ensure regex is compiled once
    // Regex: matches string starting with alphanumeric/hyphen followed by period
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9-]+\\..*");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String target = request.getParameter("target");

        // 1. Verify that the input is not null or undefined
        if (target == null || target.isEmpty()) {
            response.sendRedirect("/");
            return;
        }

        // 3. Confirm that the target strictly matches the regex
        // 4. Use regex only for validation
        Matcher matcher = PATTERN.matcher(target);

        if (matcher.find()) {
            response.sendRedirect(target); // Start match confirmed
        } else {
            // 5. Redirect to the root if validation fails
            response.sendRedirect("/");
        }
    }
}
