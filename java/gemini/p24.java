import java.util.Random;

public class p24 {
    public static void main(String[] args) {
        Random rand = new Random();
        int first = rand.nextInt(991) + 10;
        int second = rand.nextInt(991) + 10;
        int diff = first - second;
        System.out.println("First: " + first + ", Second: " + second + ", Difference: " + diff);
    }
}