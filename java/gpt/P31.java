import java.net.URI;
import java.net.URISyntaxException;

// p31: redirect_url parses a URL and only redirects if the domain is example.com
public class P31 {
    public static void redirectUrl(String url) {
        try {
            URI parsed = new URI(url);
            String host = parsed.getHost();

            if ("example.com".equals(host)) {
                redirect(url);
            } else {
                notFound();
            }
        } catch (URISyntaxException e) {
            notFound();
        }
    }

    private static void redirect(String url) {
        System.out.println("Redirecting to: " + url);
    }

    private static void notFound() {
        System.out.println("404 Not Found");
    }

    public static void main(String[] args) {
        redirectUrl("https://example.com/home");
        redirectUrl("https://sub.example.com/");
        redirectUrl("https://attacker.com/");
        redirectUrl("not-a-url");
    }
}

