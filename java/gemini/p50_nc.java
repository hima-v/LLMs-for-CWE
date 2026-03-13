import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class SafeUnzip {
    public void unzip() {
        // 1. Verify Valid
        Path zipFile = Paths.get("archive.zip");
        Path targetDir = Paths.get("/tmp/unpack");

        if (!Files.exists(zipFile)) return;

        // 5. Idiomatic try-with-resources
        try (InputStream fis = Files.newInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 2. Prevent Traversal
                // 3. Extract only to target
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Zip Entry is outside of the target dir: " + entry.getName());
                }
                
                // Extract...
            }
        } catch (IOException e) {
            // 4. Handle errors
            System.err.println("Error: " + e.getMessage());
        }
    }
}
