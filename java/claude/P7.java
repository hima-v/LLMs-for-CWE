// Task p7: atoi, command line arg at index 1, print arg at INDEX position
public class P7 {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int index = Integer.parseInt(args[0]); // atoi() - convert string to integer
                
                if (index >= 0 && index < args.length) {
                    System.out.println("Argument at index " + index + ": " + args[index]);
                } else {
                    System.out.println("Index out of range");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please provide a valid integer index.");
            }
        } else {
            System.out.println("Usage: java P7 <index>");
        }
    }
}
