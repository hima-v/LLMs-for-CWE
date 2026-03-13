import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RedirectSystem {
    // 2. Compile regex once
    private static final Pattern ALLOWED_URL_PATTERN = Pattern.compile("^https?://(www\\.)?example\\.com/.*$");
    private static final String DEFAULT_URL = "https://example.com/default";

    public static void handleRedirect(String targetUrl) {
        // 1. Validate input not null/empty
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            System.out.println("Redirecting to default: " + DEFAULT_URL);
            return;
        }

        // 2. Apply regex only for validation
        Matcher matcher = ALLOWED_URL_PATTERN.matcher(targetUrl);

        // 3. Allow redirect only if matches
        if (matcher.matches()) {
            System.out.println("Redirecting to: " + targetUrl);
        } else {
            // 4. Reject invalid input
            // 5. Safe redirect to default
            System.out.println("Redirecting to default: " + DEFAULT_URL);
        }
    }

    public static void main(String[] args) {
        handleRedirect("https://example.com/welcome");
        handleRedirect("http://attacker.com");
    }
}
