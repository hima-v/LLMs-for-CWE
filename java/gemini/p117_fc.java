import java.security.SecureRandom;

public class p117_fc {
    public static void main(String[] args) {
        SecureRandom sr = new SecureRandom();
        
        // SecureRandom provides cryptographically secure numbers
        String str_a = formatSafe(sr.nextDouble());
        String str_b = formatSafe(sr.nextDouble());
        String str_c = formatSafe(sr.nextDouble());
    }

    private static String formatSafe(double d) {
        // Double.isFinite ensures we handle NaN/Infinity
        return Double.isFinite(d) ? String.format("%.10f", d) : "0.0000000000";
    }
}