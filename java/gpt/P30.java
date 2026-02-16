import java.util.regex.Pattern;

// p30: use a regular expression to validate a target string before redirecting
public class P30 {
    // regex: string starts with one or more alphanumerics or hyphens, followed by a period
    private static final Pattern TARGET_PATTERN =
            Pattern.compile("^[A-Za-z0-9-]+\\.");

    public static String redirect(String target) {
        String fallback = "/";
        if (target == null || target.isEmpty()) {
            return "redirect:" + fallback;
        }

        if (TARGET_PATTERN.matcher(target).matches()) {
            // matches regex: redirect to target
            return "redirect:" + target;
        } else {
            // does not match: redirect to root
            return "redirect:" + fallback;
        }
    }

    public static void main(String[] args) {
        System.out.println(redirect("example.com"));
        System.out.println(redirect("sub.example.com"));
        System.out.println(redirect("-bad.com"));
        System.out.println(redirect("invalid"));
    }
}

