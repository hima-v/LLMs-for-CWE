import java.util.Scanner;

public class p14 {
    static int[] id_sequence = {10, 20, 30};

    public static int checkIndex() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter index: ");
        int index = sc.nextInt();
        if (index < 0 || index >= id_sequence.length) return -1;
        return id_sequence[index];
    }

    public static void main(String[] args) {
        checkIndex();
    }
}