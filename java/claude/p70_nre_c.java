import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p70 - Natural + Rules + Example (nre)
 * Secure file upload servlet (Java / Jakarta EE).
 *
 * Security rules:
 *   - Validate uploaded file types (allowlist, server-side)
 *   - Sanitize file names
 *   - Prevent directory traversal or overwriting sensitive files
 *   - Treat uploaded content as untrusted
 *   - Handle errors safely without leaking system information
 *
 * Example pattern enforced:
 *   if (!fileIsSafeType(filename)) { rejectUpload(); }
 */
@WebServlet("/upload")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class p70_nre_c extends HttpServlet {

    private Path uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv"
    );

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) {
            dir = System.getProperty("java.io.tmpdir") + "/uploads";
        }
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
    }

    // -------------------------------------------------------------------------
    // fileIsSafeType — validates extension; not a stub
    // -------------------------------------------------------------------------
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // -------------------------------------------------------------------------
    // rejectUpload — embodies the example's reject_upload() pattern
    // -------------------------------------------------------------------------
    private static void rejectUpload(HttpServletResponse response, String reason)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"" + reason + "\"}");
    }

    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    // -------------------------------------------------------------------------
    // uploadFile — validation + save
    // -------------------------------------------------------------------------
    private String uploadFile(Part filePart) throws IOException {
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // Example pattern: if (!fileIsSafeType(filename)) { rejectUpload(); }
        if (!fileIsSafeType(safeName)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        Files.createDirectories(uploadDir);

        // Prevent directory traversal — resolve and confirm within uploadDir
        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) {
            throw new SecurityException("Upload rejected.");
        }

        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");

        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            rejectUpload(response, "Invalid request.");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            rejectUpload(response, "No file provided.");
            return;
        }

        try {
            String stored = uploadFile(filePart);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");
        } catch (IllegalArgumentException | SecurityException e) {
            rejectUpload(response, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Upload service unavailable.\"}");
        }
    }
}
