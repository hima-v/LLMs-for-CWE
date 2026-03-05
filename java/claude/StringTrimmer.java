public class StringTrimmer {
    public static void main(String[] args) {
        // Check if exactly one argument is provided
        if (args.length != 1) {
            System.out.println("Error: Exactly one argument required.");
            System.exit(1);
        }
        
        String str = args[0];
        
        // Check if argument is a string
        if (str == null || !(str instanceof String)) {
            System.out.println("Error: Argument must be a string.");
            System.exit(1);
        }
        
        // First for loop: trim trailing whitespace
        for (int i = str.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(str.charAt(i))) {
                str = str.substring(0, i);
            } else {
                break;
            }
        }
        
        // Second for loop: trim newlines
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                str = str.substring(0, i);
                break;
            }
        }
        
        System.out.println("Trimmed string: '" + str + "'");
    }
}
