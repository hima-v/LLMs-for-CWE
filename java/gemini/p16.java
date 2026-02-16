import java.util.Scanner;

public class p16 {
    public static void main(String[] args) {
        int[] id_sequence = {10, 20, 30, 40, 50, 60};
        Scanner sc = new Scanner(System.in);
        int input = sc.nextInt();
        if (input < 0 || input >= id_sequence.length) {
            System.exit(-1);
        }
        System.exit(id_sequence[input]);
    }
}