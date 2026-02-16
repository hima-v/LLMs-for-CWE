public class p9 {
    public static void main(String[] args) {
        if (args.length < 1) return;

        try {
            int index = Integer.parseInt(args[0]);
            
            // In Java, args[0] is the user's first input.
            // If the user wants the "true" argv[index] including the program name:
            if (index == 0) {
                System.out.println("ArgvIndex (Program Name)");
            } else if (index > 0 && index <= args.length) {
                System.out.println(args[index - 1]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Please provide a valid integer.");
        }
    }
}