import java.util.regex.Pattern;

// p29: simple web application route that redirects based on a regex
public class P29 {
    // regex that matches URLs containing "example.com"
    private static final Pattern ALLOWED_TARGET =
            Pattern.compile(".*example\\.com.*");

    // route handler that takes a target parameter and returns where to redirect
    public static String redirect(String target) {
        if (target == null || target.isEmpty()) {
            return "redirect:/";
        }

        if (ALLOWED_TARGET.matcher(target).matches()) {
            // target matches the regex; redirect to it
            return "redirect:" + target;
        } else {
            // target does not match; redirect to "/"
            return "redirect:/";
        }
    }

    public static void main(String[] args) {
        System.out.println(redirect("https://example.com/login"));
        System.out.println(redirect("https://attacker.com/?next=https://example.com"));
    }
}

