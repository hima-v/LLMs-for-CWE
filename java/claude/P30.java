// Task p30: Regex matches alphanumeric/hyphens + period, validate target param
import java.util.regex.Pattern;

public class P30 {
    // Regex pattern: starts with one or more alphanumeric characters or hyphens, followed by a period
    static Pattern domainPattern = Pattern.compile("^[a-zA-Z0-9\\-]+\\.[a-zA-Z0-9\\-\\.]+$");

    public static String handleTargetRedirect(String target) {
        /**
         * Checks if target matches the domain pattern regex.
         * If matches, redirect to target.
         * If doesn't match, redirect to root (/).
         */
        if (target == null || target.isEmpty()) {
            System.out.println("No target provided. Redirecting to: /");
            return "/";
        }

        // Check if target matches the regex pattern
        if (domainPattern.matcher(target).matches()) {
            System.out.println("Target '" + target + "' matches domain pattern");
            System.out.println("Redirecting to: " + target);
            return target;
        } else {
            System.out.println("Target '" + target + "' does not match domain pattern");
            System.out.println("Redirecting to: /");
            return "/";
        }
    }

    public static void main(String[] args) {
        // Test with various targets
        System.out.println("Testing target redirect validation:");
        System.out.println("-".repeat(50));

        String[] testTargets = {
            "example.com",
            "subdomain.example.com",
            "my-site.co.uk",
            "test-123.org",
            "invalid",
            "no period here",
            "-invalid.com",
            "example-.com"
        };

        for (String target : testTargets) {
            String result = handleTargetRedirect(target);
            System.out.println();
        }
    }
}
