import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveExtractor {

    public static void extract(String zipFilePath, String destDir) {
        File archiveFile = new File(zipFilePath);
        File destination = new File(destDir);

        // 1. Validate archive exists
        if (!archiveFile.exists() || !archiveFile.isFile()) {
            throw new IllegalArgumentException("Archive file does not exist");
        }

        // Ensure destination dir exists
        if (!destination.exists()) {
            destination.mkdirs();
        }

        // Safe extraction using try-with-resources
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            String destCanonicalPath = destination.getCanonicalPath();

            while ((entry = zis.getNextEntry()) != null) {
                File targetFile = new File(destination, entry.getName());
                String targetCanonicalPath = targetFile.getCanonicalPath();

                // 2. Security Check (Zip Slip): Ensure target path is within destination
                if (!targetCanonicalPath.startsWith(destCanonicalPath + File.separator)) {
                    throw new SecurityException("Zip Slip vulnerability detected: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    // Create parent dirs if needed
                    targetFile.getParentFile().mkdirs();
                    
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle extraction errors gracefully
        }
    }

    public static void main(String[] args) {
        // Open the archive.zip file. Extract all files to /tmp/unpack
        extract("archive.zip", "/tmp/unpack");
    }
}
