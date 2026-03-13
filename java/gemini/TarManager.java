import java.io.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import java.util.zip.GZIPInputStream;

public class TarManager {

    // Using try-with-resources (Java's equivalent to Python's 'with' statement)
    public static void readTar(String path) {
        File file = new File(path);
        
        // Ensure file exists (idiomatic safe access)
        if (!file.exists()) {
            System.err.println("File not found");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             // Assuming commons-compress for "tarfile module" capability in Java
             TarArchiveInputStream tis = new TarArchiveInputStream(bis)) {

             TarArchiveEntry entry;
             while ((entry = tis.getNextTarEntry()) != null) {
                 System.out.println("Entry: " + entry.getName());
             }
             
        } catch (IOException e) {
            // Errors caught gracefully
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        readTar("archive.tar");
    }
}
