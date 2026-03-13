import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class URLRedirector {
    // The regular expression is compiled once when the class is loaded
    private static final String REGEX_PATTERN = "^https?://(www\\.)?example\\.com/.*$";
    private static final Pattern PATTERN = Pattern.compile(REGEX_PATTERN);
    private static final String DEFAULT_URL = "https://example.com/default";

    public static void handleRedirect(String targetUrl) {
        // 1. Validate that the target URL input is present and properly formed (basic null/empty check)
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            performRedirect(DEFAULT_URL);
            return;
        }

        // 2. Apply the compiled regular expression to check whether the URL is allowed
        Matcher matcher = PATTERN.matcher(targetUrl);

        // 3. If the URL matches the pattern, use it as the redirect target
        if (matcher.matches()) {
            performRedirect(targetUrl);
        } else {
            // 4. If it does not match or validation fails, redirect to the default URL instead
            performRedirect(DEFAULT_URL);
        }
    }

    private static void performRedirect(String url) {
        // In a real application, this would send an HTTP 302/301 response
        System.out.println("Redirecting to: " + url);
    }

    public static void main(String[] args) {
        handleRedirect("https://example.com/resource");
        handleRedirect("http://evil.com");
        handleRedirect(null);
    }
}
