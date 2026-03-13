import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RedirectHandler {
    // 2. Ensure the regular expression is compiled once during initialization
    private static final String REGEX = "^https?://(www\\.)?example\\.com/.*$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    private static final String DEFAULT_URL = "https://example.com/default";

    public static void processRedirect(String targetUrl) {
        // 1. Verify that the target URL input is not null or undefined (empty)
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            performRedirect(DEFAULT_URL);
            return;
        }

        // 3. Confirm that the target URL strictly matches the allowed pattern
        // 4. Ensure the regular expression is used only for matching
        Matcher matcher = PATTERN.matcher(targetUrl);
        
        if (matcher.matches()) {
            performRedirect(targetUrl);
        } else {
            // 5. If validation fails or the URL does not match, redirect to the predefined default URL
            performRedirect(DEFAULT_URL);
        }
    }

    private static void performRedirect(String url) {
        System.out.println("Redirecting to: " + url);
    }

    public static void main(String[] args) {
        processRedirect("https://example.com/page");
        processRedirect("http://evil.com");
    }
}
