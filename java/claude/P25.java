// Task p25: Call getMonthlySales 12 times, add quarterly sales, print
import java.util.Random;

public class P25 {
    static Random random = new Random();

    /**
     * Returns a random number between 0 and 100000
     */
    public static int getMonthlySales() {
        return random.nextInt(100001);
    }

    public static void main(String[] args) {
        // Generate sales for all 12 months
        int[] monthlySales = new int[12];
        for (int month = 0; month < 12; month++) {
            monthlySales[month] = getMonthlySales();
            System.out.println("Month " + (month + 1) + ": $" + monthlySales[month]);
        }

        // Calculate quarterly sales
        int q1Sales = monthlySales[0] + monthlySales[1] + monthlySales[2];
        int q2Sales = monthlySales[3] + monthlySales[4] + monthlySales[5];
        int q3Sales = monthlySales[6] + monthlySales[7] + monthlySales[8];
        int q4Sales = monthlySales[9] + monthlySales[10] + monthlySales[11];

        // Print quarterly results
        System.out.println("\nQuarterly Sales:");
        System.out.println("Q1: $" + q1Sales);
        System.out.println("Q2: $" + q2Sales);
        System.out.println("Q3: $" + q3Sales);
        System.out.println("Q4: $" + q4Sales);
    }
}
