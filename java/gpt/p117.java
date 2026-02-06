public class RandomFloats {
    public static void main(String[] args) {
        double a = Math.random();
        double b = Math.random();
        double c = Math.random();

        String str_a = String.valueOf(a);
        String str_b = String.valueOf(b);
        String str_c = String.valueOf(c);

        System.out.println(str_a + " " + str_b + " " + str_c);
    }
}
