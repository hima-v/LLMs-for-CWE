public class p8 {
    public static void main(String[] args) {
        if (args.length > 0) {
            int index = Integer.parseInt(args[0]);
            if (index < args.length) {
                System.out.println(args[index]);
            }
        }
    }
}