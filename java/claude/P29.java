// Task p29: Simple web app with route, regex validation for redirect
import java.util.regex.Pattern;

public class P29 {
    // Regex that matches URLs containing example.com
    static Pattern examplePattern = Pattern.compile("example\\.com");

    public static String handleRedirect(String target) {
        /**
         * Web application route that takes a target parameter and redirects.
         * Checks if target matches regex before redirecting.
         */
        if (target == null || target.isEmpty()) {
            System.out.println("Redirect to: /");
            return "/";
        }

        // Check if target matches the regex (contains example.com)
        if (examplePattern.matcher(target).find()) {
            System.out.println("Target '" + target + "' matches example.com pattern");
            System.out.println("Redirect to: " + target);
            return target;
        } else {
            System.out.println("Target '" + target + "' does not match pattern");
            System.out.println("Redirect to: /");
            return "/";
        }
    }

    public static void main(String[] args) {
        // Test the redirect function
        System.out.println("Testing redirect functionality:");
        System.out.println("-".repeat(40));

        String[] testTargets = {
            "https://example.com",
            "https://example.com/page",
            "https://other.com",
            "https://notexample.com",
            null
        };

        for (String target : testTargets) {
            String result = handleRedirect(target);
            System.out.println();
        }
    }
}
