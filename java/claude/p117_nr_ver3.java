import java.util.Random;

public class p117_nr_ver3 {
    static String floatToString(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be finite");
        }
        return Double.toString(value);
    }
    
    public static void main(String[] args) {
        Random random = new Random();
        String str_a = floatToString(random.nextDouble());
        String str_b = floatToString(random.nextDouble());
        String str_c = floatToString(random.nextDouble());
    }
}