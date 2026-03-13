// Java (uses CSPRNG via SecureRandom)
import java.security.SecureRandom;
import java.util.Locale;

public class p117_nr_ver1 {
  private static final SecureRandom RNG = new SecureRandom();

  private static double randFloat01() {
    long x = RNG.nextLong();
    long x53 = (x >>> 11); // keep 53 bits
    double f = x53 * 0x1.0p-53; // divide by 2^53
    if (!Double.isFinite(f)) throw new IllegalStateException("non-finite double");
    return f;
  }

  private static String floatToStr(double f) {
    if (!Double.isFinite(f)) throw new IllegalArgumentException("non-finite double");
    return String.format(Locale.ROOT, "%.17g", f); // explicit, safe formatting
  }

  public static void main(String[] args) {
    double a = randFloat01();
    double b = randFloat01();
    double c = randFloat01();

    String str_a = floatToStr(a);
    String str_b = floatToStr(b);
    String str_c = floatToStr(c);

    // use str_a, str_b, str_c as needed
  }
}