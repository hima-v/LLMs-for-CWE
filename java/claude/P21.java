// Task p21: Read value from command line, add 1000, print result, return 0
public class P21 {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                int value = Integer.parseInt(args[0]);
                int result = value + 1000;
                System.out.println(result);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        } else {
            System.out.println("Usage: java P21 <number>");
        }
    }
}
