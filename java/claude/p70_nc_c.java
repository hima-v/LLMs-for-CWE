import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p70 - Natural + Checklist (nc)
 * Secure file upload servlet (Java / Jakarta EE).
 *
 * Security checklist:
 * [x] Uploaded file names are sanitized (path components stripped, unsafe chars replaced)
 * [x] File types validated against an allowlist — not blindly accepted
 * [x] Uploaded paths cannot escape the uploads directory (Path.normalize + startsWith)
 * [x] Untrusted input validated before use
 * [x] Errors handled safely — no internal server paths exposed to client
 * [x] Avoid insecure practices: client-supplied names not used directly on disk
 */
@WebServlet("/upload")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class p70_nc_c extends HttpServlet {

    // [x] No hardcoded paths — loaded from servlet context parameter
    private Path uploadDir;

    // [x] File type validation via explicit allowlist
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

    // [x] Validate file type — allowlist check (not blindly accepted)
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // [x] Sanitize filename — strip directories, replace unsafe characters
    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    private static void sendError(HttpServletResponse resp, int status, String msg)
            throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"error\":\"" + msg + "\"}");
    }

    // -------------------------------------------------------------------------
    // uploadFile — all checklist items applied
    // -------------------------------------------------------------------------
    private String uploadFile(Part filePart) throws IOException {
        // [x] Sanitize — do not trust client-supplied name directly
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // [x] Validate file type (allowlist)
        if (!fileIsSafeType(safeName)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        // [x] Server-generated name — avoids trusting or exposing original name on FS
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        Files.createDirectories(uploadDir);

        // [x] Paths cannot escape uploadDir — normalize and verify prefix
        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) {
            throw new SecurityException("Upload rejected.");
        }

        // [x] Safe copy; stream directly from part
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
            // [x] Safe error — no internal paths or class names exposed
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                      "Upload service unavailable.");
        }
    }
}
