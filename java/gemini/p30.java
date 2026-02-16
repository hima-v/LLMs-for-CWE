import java.util.regex.Pattern;

public class p30 {
    private static final Pattern VALID_START = Pattern.compile("^[a-zA-Z0-9-]+\\..*");

    public static void main(String[] args) {
        String target = "alpha-1.com"; // Simulating query param
        if (VALID_START.matcher(target).matches()) {
            System.out.println("Redirect to: " + target);
        } else {
            System.out.println("Redirect to: /");
        }
    }
}