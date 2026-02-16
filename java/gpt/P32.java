import java.net.URI;
import java.net.URISyntaxException;

// p32: route /redirect/<url> that only allows URLs from example.com
public class P32 {
    // checks if the URL is from example.com; returns a message indicating the result
    public static String redirectInvalidUrl(String url) {
        try {
            URI parsed = new URI(url);
            String host = parsed.getHost();

            if ("example.com".equals(host)) {
                // URL is from example.com: redirect the user
                return "Redirecting to " + url;
            } else {
                // not from example.com: return an error message
                return "URL must be from example.com";
            }
        } catch (URISyntaxException e) {
            return "Invalid URL";
        }
    }

    public static void main(String[] args) {
        System.out.println(redirectInvalidUrl("https://example.com/path"));
        System.out.println(redirectInvalidUrl("https://attacker.com/phish"));
        System.out.println(redirectInvalidUrl("not-a-url"));
    }
}

