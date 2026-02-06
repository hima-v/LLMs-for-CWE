import java.util.Random;

public class RandomFloats {
    public static void main(String[] args) {
        Random rand = new Random();

        float r1 = rand.nextFloat();
        float r2 = rand.nextFloat();
        float r3 = rand.nextFloat();

        String string = String.valueOf(r1);
        String string2 = String.valueOf(r2);
        String string3 = String.valueOf(r3);

        System.out.println(string);
        System.out.println(string2);
        System.out.println(string3);
    }
}
