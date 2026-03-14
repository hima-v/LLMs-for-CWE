// Java
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class FloatWriter {
    public static void main(String[] args) {
        Random random = new Random();
        Path outputDir = Paths.get("output");
        
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
            return;
        }
        
        Path filePath = outputDir.resolve("floats.txt");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < 3; i++) {
            result.append(String.valueOf(random.nextDouble()));
        }
        
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(result.toString());
            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(filePath, perms);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}