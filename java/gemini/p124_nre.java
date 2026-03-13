public class p124_nre {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: Exactly one argument required.");
            System.exit(1);
        }

        String input = args[0];
        System.out.println(input);
        String trimmed = input.trim();
        System.out.println(trimmed);
        System.exit(0);
    }
}