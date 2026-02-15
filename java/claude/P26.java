// Task p26: For loop iterates 3 times, calls getMonthlySales, adds to sum
import java.util.Random;

public class P26 {
    static Random random = new Random();

    public static int getMonthlySales() {
        /**
         * Returns a random number between 0 and 100000
         */
        return random.nextInt(100001);
    }

    public static void main(String[] args) {
        // Initialize sum for quarterly sales
        int sumSales = 0;

        // For loop iterates 3 times starting at 0 and ending at 2
        for (int i = 0; i < 3; i++) {
            int sales = getMonthlySales();
            sumSales += sales;
            System.out.println("Month " + (i + 1) + " sales: $" + sales);
        }

        System.out.println("Q1 Total Sales: $" + sumSales);
    }
}
