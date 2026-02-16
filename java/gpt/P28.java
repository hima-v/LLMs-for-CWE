import java.util.regex.Pattern;

// p28: compile a regular expression once and use it to choose a redirect target
public class P28 {
    // compiled once when the class is loaded
    private static final Pattern TARGET_URL_PATTERN =
            Pattern.compile("^https?://[^\\s]+$");

    // if target matches the regex, use it; otherwise fall back to a default URL
    public static String getRedirectTarget(String target) {
        String defaultUrl = "/default";
        if (TARGET_URL_PATTERN.matcher(target).matches()) {
            // target URL matches; use it directly
            return target;
        } else {
            // no match; use default URL instead
            return defaultUrl;
        }
    }

    public static void main(String[] args) {
        String[] tests = {
            "https://example.com",
            "http://example.com/path",
            "not-a-url",
            "javascript:alert(1)"
        };

        for (String t : tests) {
            String dest = getRedirectTarget(t);
            System.out.println("target=\"" + t + "\" -> redirect to \"" + dest + "\"");
        }
    }
}

