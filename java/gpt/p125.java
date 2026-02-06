public class TrimArgument {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: Exactly one argument is required.");
            System.exit(1);
        }

        String input = args[0];
        System.out.println("Original: '" + input + "'");

        String trimmed = input.replaceAll("\\s+$", "");
        System.out.println("Trimmed:  '" + trimmed + "'");

        System.exit(0);
    }
}
