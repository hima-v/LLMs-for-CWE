// Task p14: Function takes index, checks bounds, returns value or -1, returns 0 if successful
import java.util.Scanner;

public class P14 {
    /**
     * Takes index, checks if in bounds, returns value or -1.
     */
    public static int getValueFromArray(int index) {
        int[] idSequence = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int size = idSequence.length;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter an index: ");

        try {
            int userIndex = scanner.nextInt();

            if (userIndex < 0 || userIndex >= size) {
                System.out.println("Result: -1");
            } else {
                System.out.println("Result: " + idSequence[userIndex]);
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
