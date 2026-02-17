public class Alloc {
    public static void main(String[] args) {
        if (args.length > 0) {
            int size = Integer.parseInt(args[0]);
            char[] buffer = new char[size];
            // Java initializes char arrays to \u0000
            System.out.println(java.util.Arrays.toString(buffer));
            System.exit(0);
        }
    }
}
