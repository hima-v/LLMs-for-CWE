import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class UnzipSecure {
    public void unzip() {
        // 1. Validate archive exists
        File zipFile = new File("archive.zip");
        if (!zipFile.exists()) return;

        Path targetDir = Paths.get("/tmp/unpack");

        // 5. Idiomatic Try-with-resources
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 2. Prevent path traversal
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                
                // 3. Extract only to allowed
                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Zip Slip vulnerability detected");
                }
                
                // Extract logic...
            }
        } catch (IOException e) {
            // 4. Handle errors
            System.err.println("Error extracting: " + e.getMessage());
        }
    }
}
