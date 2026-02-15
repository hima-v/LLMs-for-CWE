// Task p10: getValueFromArray function with bounds checking
import java.util.Scanner;

public class P10 {
    /**
     * Returns the value at the given index, or -1 if out of bounds.
     * 
     * arr: array of integers
     * size: size of the array
     * index: index to access
     */
    public static int getValueFromArray(int[] arr, int size, int index) {
        if (index < 0 || index >= size) {
            return -1;
        }
        return arr[index];
    }
    
    public static void main(String[] args) {
        int[] idSequence = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int size = idSequence.length;
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an index: ");
        
        try {
            int userIndex = scanner.nextInt();
            int result = getValueFromArray(idSequence, size, userIndex);
            System.out.println("Value at index " + userIndex + ": " + result);
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid integer.");
        }
        
        scanner.close();
    }
}
