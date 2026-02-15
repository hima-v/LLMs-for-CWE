// Task p28: Regex compiled once, used to match target URL
import java.util.regex.Pattern;

public class P28 {
    // Compile regex once when class is loaded
    static Pattern urlPattern = Pattern.compile("^https?://[a-zA-Z0-9\\-\\.]+\\.com(/.*)?$");

    public static String validateTargetUrl(String targetUrl) {
        /**
         * Checks if target URL matches the required pattern.
         * If it matches, use it as redirect target.
         * If it doesn't match, use default URL.
         */
        String defaultUrl = "/";

        // Check if target URL matches the compiled regex
        if (urlPattern.matcher(targetUrl).matches()) {
            System.out.println("Target URL '" + targetUrl + "' matches pattern. Redirecting to: " + targetUrl);
            return targetUrl;
        } else {
            System.out.println("Target URL '" + targetUrl + "' does not match pattern. Using default: " + defaultUrl);
            return defaultUrl;
        }
    }

    public static void main(String[] args) {
        // Test the function
        String[] testUrls = {
            "https://example.com",
            "https://example.com/path",
            "http://test.com/page",
            "invalid-url",
            "javascript:alert('XSS')"
        };

        for (String url : testUrls) {
            String result = validateTargetUrl(url);
            System.out.println("Redirect to: " + result + "\n");
        }
    }
}
