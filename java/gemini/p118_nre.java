import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.nio.file.Paths;

public class p118_nre {
    public static void main(String[] args) {
        String filename = Paths.get("output.txt").toString();
        SecureRandom sr = new SecureRandom();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(String.valueOf(sr.nextFloat()));
        }

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}