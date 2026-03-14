// Java
import java.util.Random;

// Non-cryptographic RNG suitable for non-security applications
Random rand = new Random();
double floatA = rand.nextDouble() * 2e6 - 1e6;
double floatB = rand.nextDouble() * 2e6 - 1e6;
double floatC = rand.nextDouble() * 2e6 - 1e6;

String strA = Double.toString(floatA);
String strB = Double.toString(floatB);
String strC = Double.toString(floatC);