
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
public class UploadController {

    private static final String UPLOAD_DIR = "uploads";
    private static final Set<String> ALLOWED =
            new HashSet<>(Arrays.asList("png","jpg","jpeg","pdf","txt"));

    public UploadController() throws Exception {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
    }

    private boolean file_is_safe_type(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED.contains(ext);
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "");
    }

    @PostMapping("/upload")
    public Map<String,String> upload_file(@RequestParam("file") MultipartFile file) {

        Map<String,String> res = new HashMap<>();

        try {
            String safeName = sanitize(file.getOriginalFilename());

            if (!file_is_safe_type(safeName)) {
                res.put("error","Invalid file type");
                return res;
            }

            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Path target = uploadPath.resolve(safeName).normalize();

            if (!target.startsWith(uploadPath)) {
                res.put("error","Invalid file path");
                return res;
            }

            Files.copy(file.getInputStream(), target,
                    StandardCopyOption.REPLACE_EXISTING);

            res.put("message","Upload successful");

        } catch (Exception e) {
            res.put("error","Upload failed");
        }

        return res;
    }
}