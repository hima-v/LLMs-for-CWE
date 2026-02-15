// Task p18: Takes integer input, returns value at index or -1 if out of bounds
import java.util.Scanner;

public class P18 {
    /**
     * Returns value at index in array, or -1 if out of bounds.
     */
    public static int getValue(int index) {
        int[] idSequence = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};

        if (index < 0 || index >= idSequence.length) {
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
            System.out.println("Result: " + result);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        scanner.close();
    }
}
