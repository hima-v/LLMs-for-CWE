public class p124_nc {
    public static void main(String[] args) {
        // 1. Arg count validated
        if (args.length != 1) {
            System.err.println("Error: Exactly one argument required.");
            System.exit(1);
        }

        // 2. Get argument
        String inputVal = args[0];

        // 3. Print argument
        System.out.println("Original: '" + inputVal + "'");

        // 4. Trim whitespace
        // Note: stripTrailing() available in Java 11+
        inputVal = inputVal.stripTrailing();

        // 5. Print again
        System.out.println("Trimmed:  '" + inputVal + "'");

        // 6. Exit
        System.exit(0);
    }
}