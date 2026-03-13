import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

public class p118_nr {
    public static void main(String[] args) {
        String baseDir = "/tmp/app_data";
        Path safePath = Paths.get(baseDir).resolve("output.txt").normalize();
        
        SecureRandom sr = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(String.valueOf(sr.nextDouble()));
        }
        
        try (FileWriter writer = new FileWriter(safePath.toFile())) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }
}