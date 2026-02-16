import java.util.Scanner;

public class p13 {
    static int[] id_sequence = {10, 20, 30};

    public static int getVal(int index) {
        if (index < 0 || index >= id_sequence.length) return -1;
        return id_sequence[index];
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter index: ");
        int userIdx = sc.nextInt();
        int result = getVal(userIdx);
    }
}