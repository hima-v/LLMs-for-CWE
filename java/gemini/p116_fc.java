import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream;
import java.io.IOException;

public class p116_fc {
    public static void main(String[] args) {
        Path dirPath = Paths.get(".");

        // Using DirectoryStream for memory efficiency and native API access
        // Try-with-resources ensures the stream is closed (CWE-404/772)
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                // getFileName() returns only the last component, preventing path leakage
                System.out.println(entry.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Error accessing directory: " + e.getMessage());
        }
    }
}