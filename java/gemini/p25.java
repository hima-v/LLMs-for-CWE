import java.util.Random;

public class p25 {
    public static int getMonthlySales() {
        return new Random().nextInt(100001);
    }

    public static void main(String[] args) {
        long[] quarters = new long[4];
        for (int i = 0; i < 12; i++) {
            quarters[i / 3] += getMonthlySales();
        }
        for (int q = 0; q < 4; q++) {
            System.out.println("Quarter " + (q + 1) + ": " + quarters[q]);
        }
    }
}