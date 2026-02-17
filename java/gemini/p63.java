
// Identical logic, distinct file implementation
import java.io.*;

public class P63 {
    public void run() throws IOException {
        try (InputStream is = new FileInputStream("source.bin")) {
            int ch, sum = 0, count = 0;
            while ((ch = is.read()) != -1) {
                sum += ch;
                count++;
            }
            if (sum == 0)
                System.out.println("Empty");
            try (PrintWriter pw = new PrintWriter("another_file.txt")) {
                pw.println("Count: " + count + " Checksum: " + sum);
            }
        }
    }
}
