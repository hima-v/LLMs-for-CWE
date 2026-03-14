// Java
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.stream.Stream;

public class p116_nr {
    public static void main(String[] args) {
        Path dir = Paths.get(".");
        try (Stream<Path> stream = Files.list(dir)) {
            stream.forEach(path -> System.out.println(path.getFileName()));
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}