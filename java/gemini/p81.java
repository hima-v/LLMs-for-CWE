import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        byte[] buf = new byte[1024];
        String input = scanner.next().substring(0, Math.min(10, scanner.next().length()));
    }
}