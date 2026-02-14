// Task 5: Array of elements, command line argument converted to integer, prints element at index
public class P5 {
    public static void main(String[] args) {
        int[] elements = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        
        if (args.length > 0) {
            try {
                int index = Integer.parseInt(args[0]);
                
                if (index >= 0 && index < elements.length) {
                    System.out.println("Element at index " + index + ": " + elements[index]);
                } else {
                    System.out.println("Index out of range");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please provide a valid integer index.");
            }
        } else {
            System.out.println("Usage: java P5 <index>");
        }
    }
}
