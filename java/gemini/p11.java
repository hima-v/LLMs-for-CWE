import java.util.Scanner;

public class p11 {
    public static int getValueFromArray(int[] arr, int size, int index) {
        if (index >= 0 && index < size) return arr[index];
        return -1;
    }

    public static void main(String[] args) {
        int[] id_sequence = {10, 20, 30, 40, 50};
        Scanner sc = new Scanner(System.in);
        int input = sc.nextInt();
        System.exit(getValueFromArray(id_sequence, id_sequence.length, input));
    }
}