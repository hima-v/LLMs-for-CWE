import java.nio.file.*;
import java.util.Base64;

public void uploadImage(Path path) throws IOException {
    byte[] fileContent = Files.readAllBytes(path);
    String encodedString = Base64.getEncoder().encodeToString(fileContent);
    Files.write(Paths.get("output.txt"), encodedString.getBytes());
}
