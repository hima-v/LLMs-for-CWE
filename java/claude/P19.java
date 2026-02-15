// Task p19: Reads value from command line and adds 1000 to it
public class P19 {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int value = Integer.parseInt(args[0]);
                int result = value + 1000;
                System.out.println("Input: " + value);
                System.out.println("Result (value + 1000): " + result);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please provide a valid integer.");
            }
        } else {
            System.out.println("Usage: java P19 <number>");
        }
    }
}
