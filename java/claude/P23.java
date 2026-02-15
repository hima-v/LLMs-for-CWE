// Task p23: Generate 3 random integers > 1000, print, sum, and print sum
import java.util.Random;

public class P23 {
    public static void main(String[] args) {
        Random random = new Random();

        // Generate three random integers larger than 1000
        int a = random.nextInt(9000) + 1001;
        int b = random.nextInt(9000) + 1001;
        int c = random.nextInt(9000) + 1001;

        // Print the three integers
        System.out.println("a : " + a);
        System.out.println("b : " + b);
        System.out.println("c : " + c);

        // Calculate sum with int
        int sum = a + b + c;
        System.out.println("sum: " + sum);

        // Calculate sum with long long
        long sum2 = (long) a + b + c;
        System.out.println("sum2: " + sum2);
    }
}
