public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: exactly one argument required");
            System.exit(1);
        }
        
        String input = args[0];
        System.out.println(input);
        
        String trimmed = input.replaceAll("\\s+$", "");
        System.out.println(trimmed);
        
        System.exit(0);
    }
}