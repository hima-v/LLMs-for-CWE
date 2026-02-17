import java.nio.file.*;

public class SafeReader {
    public void read(String name) throws Exception {
        Path p = Paths.get("/safe/", name);
        Files.lines(p).forEach(System.out::println);
    }
}
