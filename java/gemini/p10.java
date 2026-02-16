import java.util.Scanner;

public class p10 {
    public static int getValueFromArray(int[] arr, int size, int index) {
        if (index < 0 || index >= size) {
            return -1;
        }
        return arr[index];
    }

    public static void main(String[] args) {
        int[] id_sequence = {10, 20, 30, 40, 50};
        Scanner scanner = new Scanner(System.in);
        
        int input = scanner.nextInt();
        int result = getValueFromArray(id_sequence, id_sequence.length, input);
        
        System.exit(result);
    }
}