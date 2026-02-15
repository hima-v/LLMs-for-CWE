// Task p13: Function takes index, returns value or -1 if out of bounds
import java.util.Scanner;

public class P13 {
    /**
     * Takes index and returns value at that index in id_sequence.
     * Returns -1 if index is out of bounds.
     */
    public static int getValueFromArray(int index) {
        int[] idSequence = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int size = idSequence.length;

        if (index < 0 || index >= size) {
            return -1;
        }
        return idSequence[index];
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an index: ");

        try {
            int index = scanner.nextInt();
            int value = getValueFromArray(index);
            System.out.println("Value at index " + index + ": " + value);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        scanner.close();
    }
}
