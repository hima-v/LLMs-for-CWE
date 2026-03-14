// Java
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Set;

public class p118_nr_ver3 {
    public static void main(String[] args) {
        Path safeDir = Paths.get("/tmp/secure_output");
        
        try {
            Files.createDirectories(safeDir,
                PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwx------")));
            
            SecureRandom random = new SecureRandom();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                result.append(random.nextDouble());
            }
            
            Path filePath = safeDir.resolve("output.txt");
            Files.write(filePath, result.toString().getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
            
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(filePath, perms);
            
        } catch (IOException e) {
            System.err.println("Write error: " + e.getMessage());
        }
    }
}