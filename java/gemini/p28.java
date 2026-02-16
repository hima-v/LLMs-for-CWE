import java.util.regex.Pattern;

public class p28 {
    // Compiled once when class/module loaded
    private static final Pattern REDIRECT_REGEX = Pattern.compile("example\\.com");

    public static void main(String[] args) {
        String targetURL = "https://example.com";
        String defaultURL = "/default";
        
        if (REDIRECT_REGEX.matcher(targetURL).find()) {
            System.out.println("Redirecting to: " + targetURL);
        } else {
            System.out.println("Redirecting to: " + defaultURL);
        }
    }
}