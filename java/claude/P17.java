// Task p17: Takes number from user, returns value at that index, -1 if out of bounds
import java.util.Scanner;

public class P17 {
    /**
     * Takes index and returns value at that index.
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
        System.out.print("Enter a number: ");

        try {
            int number = scanner.nextInt();
            int result = getValueFromArray(number);
            System.out.println("Value: " + result);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        scanner.close();
    }
}
