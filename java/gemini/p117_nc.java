import java.util.Random;

public class p117_nc {
    public static String convertToString(double val) {
        return String.format("%.4f", val);
    }

    public static void main(String[] args) {
        Random rand = new Random();
        String str_a = convertToString(rand.nextDouble());
        String str_b = convertToString(rand.nextDouble());
        String str_c = convertToString(rand.nextDouble());
    }
}