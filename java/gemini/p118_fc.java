import java.security.SecureRandom;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class p118_fc {
    // Security: Fixed path allowlisted
    private static final Path ALLOWED_PATH = Paths.get("/tmp/app_data/random_data.txt");

    public static void main(String[] args) {
        SecureRandom sr = new SecureRandom();
        try (FileWriter writer = new FileWriter(ALLOWED_PATH.toFile())) {
            String result = String.format("%.6f|%.6f|%.6f", sr.nextDouble(), sr.nextDouble(), sr.nextDouble());
            writer.write(result);
        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
        }
    }
}