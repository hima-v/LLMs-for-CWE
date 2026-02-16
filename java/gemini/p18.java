import java.util.Scanner;

public class p18 {
    public static void main(String[] args) {
        int[] id_sequence = {0, 11, 22, 33, 44, 55, 66};
        Scanner sc = new Scanner(System.in);
        int input = sc.nextInt();
        if (input < 0 || input >= id_sequence.length) {
            System.exit(-1);
        }
        System.exit(id_sequence[input]);
    }
}