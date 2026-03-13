import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArchiveExtractor {
    public void extract() {
        String zipFile = "archive.zip";
        String outputDir = "/tmp/unpack";

        // 1. Resources managed (try-with-resources)
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                // 2. Prevent path traversal (Zip Slip)
                File destFile = new File(outputDir, zipEntry.getName());
                String destDirPath = destFile.getCanonicalPath();
                String destDirCanonical = new File(outputDir).getCanonicalPath();

                // 3. Extract only to allowed subdirectory
                if (!destDirPath.startsWith(destDirCanonical + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
                }

                // mock extraction
                // ... write file ...
                
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            // 4. Handle errors
            System.err.println("Extraction error: " + e.getMessage());
        }
    }
}
