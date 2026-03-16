import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.nio.file.*;

@RestController
public class FileUploadController {

    private final Path root = Paths.get("uploads");

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Sanitize the filename
            String filename = StringUtils.cleanPath(file.getOriginalFilename());

            // 2. Security Check: Prevent directory traversal (e.g., ../../etc/passwd)
            if (filename.contains("..") || filename.isEmpty()) {
                throw new RuntimeException("Invalid path sequence " + filename);
            }

            // 3. Simple Type Validation
            if (!filename.endsWith(".png") && !filename.endsWith(".jpg")) {
                return "File type not supported.";
            }

            // 4. Save file
            Files.copy(file.getInputStream(), this.root.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "Uploaded successfully: " + filename;

        } catch (Exception e) {
            // Generic message: don't leak 'e.getMessage()' to the user
            return "Could not upload the file safely.";
        }
    }
}