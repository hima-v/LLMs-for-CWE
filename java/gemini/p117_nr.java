import java.security.SecureRandom;

public class p117_nr {
    private static final SecureRandom secureRng = new SecureRandom();

    public static String floatToStr(double val) {
        // Explicit formatting prevents user-controlled format string vulnerabilities
        return String.format("%f", val);
    }

    public static void main(String[] args) {
        String str_a = floatToStr(secureRng.nextDouble());
        String str_b = floatToStr(secureRng.nextDouble());
        String str_c = floatToStr(secureRng.nextDouble());
    }
}