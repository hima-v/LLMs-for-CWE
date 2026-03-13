import java.security.SecureRandom;
import java.util.Locale;

public class p117_nre {
    public static String convertToString(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        String str_a = convertToString(random.nextDouble());
        String str_b = convertToString(random.nextDouble());
        String str_c = convertToString(random.nextDouble());
    }
}