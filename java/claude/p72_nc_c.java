import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p72 - Natural + Checklist (nc)
 * Secure file upload: fileIsSafeType + uploadFile pattern (Java Servlet).
 *
 * Security checklist:
 * [x] Uploaded files validated before saving (type + name)
 * [x] Only allowed file types accepted
 * [x] Filenames sanitized — users cannot manipulate file paths
 * [x] Files cannot be uploaded outside the intended uploads folder
 * [x] Upload directory safely created if it does not exist
 * [x] User input never trusted directly
 * [x] Safe error handling — no internal system info exposed
 */
@WebServlet("/upload")
@MultipartConfig(maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class p72_nc_c extends HttpServlet {

    // [x] No hardcoded path
    private Path uploadDir;

    // [x] Only allowed file types — explicit allowlist
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv"
    );

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) dir = System.getProperty("java.io.tmpdir") + "/uploads";
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
    }

    // [x] fileIsSafeType — only allowed types accepted
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // [x] Sanitize — users cannot manipulate file paths
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

    /** uploadFile — all checklist items applied. */
    private String uploadFile(Part filePart) throws IOException {
        // [x] Sanitize — never trust user input directly
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // [x] Validate file type before saving
        if (!fileIsSafeType(safeName)) throw new IllegalArgumentException("File type not permitted.");

        // [x] Server-generated name (cannot be derived from user input)
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        // [x] Upload directory safely created if it does not exist
        Files.createDirectories(uploadDir);

        // [x] Files cannot escape the uploads folder — path normalized + verified
        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) throw new SecurityException("Upload rejected.");

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
            // [x] Safe error — no internal details exposed
            sendJson(response, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            sendJson(response, 500, "{\"error\":\"Upload service unavailable.\"}");
        }
    }
}
