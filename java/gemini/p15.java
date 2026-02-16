import java.util.Scanner;

public class p15 {
    static int[] id_sequence = {10, 20, 30};

    public static int checkIndex() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter index: ");
        int index = sc.nextInt();
        if (index >= 0 && index < id_sequence.length) {
            return id_sequence[index];
        }
        return 0;
    }

    public static void main(String[] args) {
        checkIndex();
    }
}