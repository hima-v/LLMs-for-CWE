import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p70 - Natural + Rules (nr)
 * Secure file upload servlet (Java / Jakarta EE).
 *
 * Rules enforced:
 *   - Validate file names and file types
 *   - Prevent directory traversal or arbitrary file writes
 *   - Avoid hardcoded paths or secrets
 *   - Handle errors safely without exposing internal details
 *   - Treat all user input as untrusted
 */
@WebServlet("/upload")
@MultipartConfig(
    maxFileSize    = 5 * 1024 * 1024,  // 5 MB per file
    maxRequestSize = 6 * 1024 * 1024   // 6 MB per request
)
public class p70_nr_c extends HttpServlet {

    // Loaded from servlet context param — no hardcoded paths
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
    // fileIsSafeType — validates extension against allowlist
    // -------------------------------------------------------------------------
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // -------------------------------------------------------------------------
    // sanitizeFilename — strips path components, replaces unsafe chars
    // -------------------------------------------------------------------------
    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    // -------------------------------------------------------------------------
    // uploadFile — core upload logic
    // -------------------------------------------------------------------------
    private String uploadFile(Part filePart) throws IOException {
        String submittedName = filePart.getSubmittedFileName();

        // Sanitize — never trust the submitted name
        String safeName = sanitizeFilename(submittedName);
        if (safeName == null) {
            throw new IllegalArgumentException("Invalid filename.");
        }

        // Validate file type
        if (!fileIsSafeType(safeName)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        // Generate a server-side unique name
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        // Ensure upload directory exists
        Files.createDirectories(uploadDir);

        // Directory traversal check — resolve and verify path stays in uploadDir
        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) {
            throw new SecurityException("Upload rejected.");
        }

        // Save file
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    // -------------------------------------------------------------------------
    // doPost — HTTP entry point
    // -------------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");

        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No file provided.");
            return;
        }

        try {
            String stored = uploadFile(filePart);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");
        } catch (IllegalArgumentException | SecurityException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Never expose internal details
            getServletContext().log("Upload error", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "Upload service unavailable.");
        }
    }

    private static void sendError(HttpServletResponse resp, int status, String msg)
            throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"error\":\"" + msg + "\"}");
    }
}
