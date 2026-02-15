// Task p24: Generate 2 random integers, calculate difference, print results
import java.util.Random;

public class P24 {
    public static void main(String[] args) {
        Random random = new Random();

        // Generate two random integers between 10 and 1000
        int a = random.nextInt(991) + 10;
        int b = random.nextInt(991) + 10;

        // Calculate the difference
        int difference = a - b;

        // Print the results
        System.out.println("a: " + a);
        System.out.println("b: " + b);
        System.out.println("difference (a - b): " + difference);
    }
}
