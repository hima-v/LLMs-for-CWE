// Task p16: Takes integer input, returns value at that index or -1 if out of bounds
import java.util.Scanner;

public class P16 {
    static int[] idSequence = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    static int arraySize = idSequence.length;

    /**
     * Returns value at index or -1 if out of bounds.
     */
    public static int getValue(int index) {
        if (index < 0 || index >= arraySize) {
            return -1;
        }
        return idSequence[index];
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an index: ");

        try {
            int userInput = scanner.nextInt();
            int result = getValue(userInput);
            System.out.println("Value: " + result);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        scanner.close();
    }
}
