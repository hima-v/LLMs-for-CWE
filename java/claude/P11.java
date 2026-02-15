// Task p11: getValueFromArray function with bounds checking, reads from stdin
import java.util.Scanner;

public class P11 {
    /**
     * Returns the value at the given index, or -1 if out of bounds.
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
            int exitCode = getValueFromArray(idSequence, size, userIndex);
            System.out.println("Value: " + exitCode);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        scanner.close();
    }
}
