import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class SecureArchive {
    public void process() {
        // 1. Validate
        Path zip = Paths.get("archive.zip");
        if (!Files.exists(zip)) return;
        Path dest = Paths.get("/tmp/unpack");

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                // 2. Prevent traversal
                // 3. Enforce dest
                Path resolved = dest.resolve(e.getName()).normalize();
                if (!resolved.startsWith(dest)) {
                    throw new SecurityException("Traversal attempt");
                }
                // Extract...
            }
        } catch (IOException | SecurityException ex) {
            // 4. Handle error
            ex.printStackTrace();
        }
    }
}
