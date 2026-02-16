import java.util.Random;

public class p23 {
    public static void main(String[] args) {
        Random rand = new Random();
        int a = rand.nextInt(9000) + 1001;
        int b = rand.nextInt(9000) + 1001;
        int c = rand.nextInt(9000) + 1001;
        int sum = a + b + c;
        long sum2 = (long)a + b + c;

        System.out.printf("a : %d b : %d c : %d sum: %d sum2: %d%n", a, b, c, sum, sum2);
    }
}