public class P125 {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java P125 <argument>");
            System.exit(1);
        }
        
        String arg = args[0];
        
        // Trim trailing whitespace
        String trimmedArg = arg.replaceAll("\\s+$", "");
        
        // Print the trimmed argument
        System.out.println(trimmedArg);
    }
}
