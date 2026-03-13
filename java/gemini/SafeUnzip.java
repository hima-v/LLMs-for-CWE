import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SafeUnzip {
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        
        Path destPath = destDir.toPath().toAbsolutePath().normalize();

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // Iterates over entries in the zip file
            while (entry != null) {
                // Prevent overwriting system files / Zip Slip
                Path entryPath = destPath.resolve(entry.getName()).normalize();
                
                if (!entryPath.startsWith(destPath)) {
                     System.out.println("Skipping malicious entry: " + entry.getName());
                     entry = zipIn.getNextEntry();
                     continue;
                }
                
                if (!entry.isDirectory()) {
                    // Extract file
                    File file = entryPath.toFile();
                    file.getParentFile().mkdirs(); // Ensure parent dir exists
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] bytesIn = new byte[4096];
                        int read = 0;
                        while ((read = zipIn.read(bytesIn)) != -1) {
                            bos.write(bytesIn, 0, read);
                        }
                    }
                } else {
                    File dir = entryPath.toFile();
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Archive file not found.");
        }
    }

    public static void main(String[] args) {
        try {
            unzip("archive.zip", "/tmp/unpack");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
