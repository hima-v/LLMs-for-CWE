import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.stream.Stream;

public class p116_nre {
    public static void main(String[] args) {
        try (Stream<Path> paths = Files.list(Paths.get("."))) {
            paths.map(Path::getFileName).forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }
}