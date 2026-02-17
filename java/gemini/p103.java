import java.nio.file.*;
import java.io.IOException;

public class FileOpener {
    public static void main(String[] args) {
        Path path = Paths.get("example.txt");
        try {
            // Mapping the flags:
            Files.write(path, "Hello Java".getBytes(), 
                StandardOpenOption.CREATE,    // O_CREAT
                StandardOpenOption.TRUNCATE_EXISTING, // O_TRUNC
                StandardOpenOption.WRITE      // O_WRONLY
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}