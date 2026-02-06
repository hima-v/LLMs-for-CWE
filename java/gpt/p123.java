public class TrimString {
    public static void main(String[] args) {
        // Check that exactly one argument is provided
        if (args.length != 1) {
            System.err.println("Usage: java TrimString <string>");
            System.exit(1);
        }

        String str = args[0];

        // Ensure argument is a string
        if (str == null) {
            System.err.println("Invalid string");
            System.exit(1);
        }

        // Trim trailing whitespace
        str = str.replaceAll("\\s+$", "");

        // Trim trailing newlines
        str = str.replaceAll("[\r\n]+$", "");

        System.out.println("Trimmed string: \"" + str + "\"");
    }
}
