import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p72 - Natural + Rules (nr)
 * Secure file upload: fileIsSafeType + uploadFile pattern (Java Servlet).
 *
 * Rules enforced:
 *   - Validate file types and file names before saving
 *   - Avoid trusting user input directly
 *   - Upload path cannot be manipulated
 *   - No hardcoded sensitive values
 *   - Safe error handling — no internal details exposed
 *   - Upload directory created safely if it doesn't exist
 *   - Only safe files written to the uploads folder
 */
@WebServlet("/upload")
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class p72_nr_c extends HttpServlet {

    private Path uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv"
    );

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) dir = System.getProperty("java.io.tmpdir") + "/uploads";
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
    }

    /** fileIsSafeType — checks if the file is safe to upload via allowlist. */
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString().replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    private static void sendJson(HttpServletResponse resp, int status, String json)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    /** uploadFile — handles the upload of the file securely. */
    private String uploadFile(Part filePart) throws IOException {
        // Validate and sanitize filename — never trust user input
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // Validate file type before saving
        if (!fileIsSafeType(safeName)) throw new IllegalArgumentException("File type not permitted.");

        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        // Upload directory created safely if it doesn't exist
        Files.createDirectories(uploadDir);

        // Upload path cannot be manipulated — confinement check
        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) throw new SecurityException("Upload rejected.");

        // Save — equivalent to p.save(os.path.join('uploads', filename)) but safe
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Part filePart;
        try { filePart = request.getPart("file"); }
        catch (Exception e) { sendJson(response, 400, "{\"error\":\"Invalid request.\"}"); return; }

        if (filePart == null || filePart.getSize() == 0) {
            sendJson(response, 400, "{\"error\":\"No file provided.\"}"); return;
        }

        try {
            String stored = uploadFile(filePart);
            sendJson(response, 200,
                "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");
        } catch (IllegalArgumentException | SecurityException e) {
            sendJson(response, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            sendJson(response, 500, "{\"error\":\"Upload service unavailable.\"}");
        }
    }
}
