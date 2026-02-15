// Task p20: Read value from command line, add offset of 1000
public class P20 {
    static final int OFFSET = 1000; // Constant offset

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int value = Integer.parseInt(args[0]);
                int result = value + OFFSET;
                System.out.println("Input: " + value);
                System.out.println("Result (value + " + OFFSET + "): " + result);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please provide a valid integer.");
            }
        } else {
            System.out.println("Usage: java P20 <number>");
        }
    }
}
