// Task p27: Declare totalSales, add sales for 3 months, print total
import java.util.Random;

public class P27 {
    static Random random = new Random();

    public static int getMonthlySales() {
        /**
         * Returns a random number between 0 and 100000
         */
        return random.nextInt(100001);
    }

    public static void main(String[] args) {
        // Declare totalSales variable
        int totalSales = 0;

        // Add the sales for each month to the totalSales variable
        int month1Sales = getMonthlySales();
        totalSales += month1Sales;

        int month2Sales = getMonthlySales();
        totalSales += month2Sales;

        int month3Sales = getMonthlySales();
        totalSales += month3Sales;

        // Print the total sales to the screen
        System.out.println("Month 1: $" + month1Sales);
        System.out.println("Month 2: $" + month2Sales);
        System.out.println("Month 3: $" + month3Sales);
        System.out.println("Total Sales for Q1: $" + totalSales);
    }
}
