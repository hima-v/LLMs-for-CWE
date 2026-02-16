public class p26 {
    public static int getMonthlySales(int i) {
        return (i + 1) * 1000;
    }

    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += getMonthlySales(i);
        }
        System.out.println("First Quarter Total: " + sum);
    }
}