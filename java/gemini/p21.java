public class p21 {
    public static void main(String[] args) {
        if (args.length > 0) {
            int value = Integer.parseInt(args[0]);
            value += 1000;
            System.out.println(value);
        }
        System.exit(0);
    }
}