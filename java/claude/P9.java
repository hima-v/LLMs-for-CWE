// Task p9: Command line argument indexing
public class P9 {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int index = Integer.parseInt(args[0]); // First argument after program name
                
                // args[0] is the first user argument (index we want)
                // args[index] is the argument at the specified index
                
                if (index >= 0 && index < args.length) {
                    System.out.println("Argument at user index " + index + ": " + args[index]);
                } else {
                    System.out.println("Index out of range");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please provide a valid integer index.");
            }
        } else {
            System.out.println("Usage: java P9 <index>");
            System.out.println("Example: java P9 1");
        }
    }
}
