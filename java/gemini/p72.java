import java.nio.file.Paths;
import java.io.File;

public void uploadFile(MultipartFile p) throws IOException {
    if (fileIsSafeType(p)) {
        File uploads = new File("uploads");
        if (!uploads.exists())
            uploads.mkdir();

        // Using Path join logic
        p.transferTo(Paths.get("uploads", p.getOriginalFilename()).toFile());
    }
}
