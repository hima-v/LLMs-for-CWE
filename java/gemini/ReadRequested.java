import java.io.File;
import java.util.Scanner;

public class ReadRequested {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String filename = scanner.nextLine();
        File file = new File("/safe/" + filename);

        Scanner fileReader = new Scanner(file);
        while (fileReader.hasNextLine()) {
            System.out.println(fileReader.nextLine());
        }
    }
}
