import java.security.SecureRandom;
import java.util.Locale;

public class p117_nr_ver2 {
    private static final SecureRandom RNG = new SecureRandom();

    private static double randomFiniteDouble() {
        // Security-sensitive: cryptographically secure RNG (SecureRandom)
        double x = RNG.nextDouble(); // [0.0, 1.0)
        if (!Double.isFinite(x)) throw new IllegalStateException("Non-finite double generated");
        return x;
    }

    public static void main(String[] args) {
        double a = randomFiniteDouble();
        double b = randomFiniteDouble();
        double c = random_finiteDouble();

        String str_a = String.format(Locale.ROOT, "%.17g", a);
        String str_b = String.format(Locale.ROOT, "%.17g", b);
        String str_c = String.format(Locale.ROOT, "%.17g", c);
    }
}