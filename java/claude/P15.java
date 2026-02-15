// Task p15: Function checks bounds, returns value if in bounds or 0 if out of bounds
import java.util.Scanner;

public class P15 {
    /**
     * Takes index, checks if in bounds.
     * Returns value if in bounds, 0 if out of bounds.
     */
    public static int getValueFromArray(int index) {
        int[] idSequence = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int size = idSequence.length;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an index: ");

        try {
            int userIndex = scanner.nextInt();

            if (userIndex >= 0 && userIndex < size) {
                System.out.println("Result: " + idSequence[userIndex]);
            } else {
                System.out.println("Result: 0");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        scanner.close();
        return 0;
    }

    public static void main(String[] args) {
        getValueFromArray(0);
    }
}
