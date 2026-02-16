public class p20 {
    public static void main(String[] args) {
        final int OFFSET = 1000;
        if (args.length > 0) {
            int val = Integer.parseInt(args[0]);
            System.out.println(val + OFFSET);
        }
    }
}