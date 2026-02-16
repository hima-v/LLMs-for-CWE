import java.util.regex.Pattern;

public class p29 {
    public static void route(String target) {
        if (Pattern.matches(".*example\\.com.*", target)) {
            System.out.println("Redirecting to: " + target);
        } else {
            System.out.println("Redirecting to: /");
        }
    }

    public static void main(String[] args) {
        route("http://example.com/login");
    }
}