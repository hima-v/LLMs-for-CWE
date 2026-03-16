import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class FileUploader {
    private static final Path ROOT = Paths.get("uploads").toAbsolutePath();
    private static final List<String> ALLOWED = Arrays.asList("jpg", "png", "pdf");

    public boolean fileIsSafeType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        return ALLOWED.contains(extension.toLowerCase());
    }

    public void uploadFile(String originalName, byte[] content) throws Exception {
        if (!fileIsSafeType(originalName)) throw new Exception("Illegal file type");

        // 1. Sanitize by getting only the filename, ignoring path components
        Path inputPath = Paths.get(originalName).getFileName();
        
        // 2. Resolve and Normalize to prevent ../ attacks
        Path destination = ROOT.resolve(inputPath).normalize();

        if (!destination.startsWith(ROOT)) {
            throw new SecurityException("Directory traversal detected!");
        }

        Files.write(destination, content);
    }
}