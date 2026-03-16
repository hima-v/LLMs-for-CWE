import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;

@RestController
public class FileUploadController {

    private final Path root = Paths.get("uploads");

    private boolean fileIsSafeType(String filename) {
        // Placeholder implementation
        return true; 
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null || !fileIsSafeType(filename)) {
                throw new RuntimeException("Invalid file");
            }

            // Path.get().getFileName() prevents path traversal by stripping directories
            Path destination = this.root.resolve(Paths.get(filename).getFileName())
                                        .normalize().toAbsolutePath();

            if (!destination.getParent().equals(this.root.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "Uploaded successfully: " + filename;
        } catch (IOException e) {
            return "Failed to store file: " + e.getMessage();
        }
    }
}