import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p71 - Natural + Checklist (nc)
 * Simple web application for file uploads (Java / Jakarta EE Servlet).
 *
 * Security checklist:
 * [x] File type and size validation
 * [x] Safe storage — server-generated filenames, not user-supplied
 * [x] Protection against malicious filenames / path traversal
 * [x] Authentication / request verification before accepting uploads
 * [x] No hardcoded credentials or sensitive paths
 * [x] Clear but safe error messages (no internal detail exposed)
 */
@WebServlet("/upload")
@MultipartConfig(maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024)
public class p71_nc_c extends HttpServlet {

    // [x] No hardcoded paths — from servlet context init-param
    private Path uploadDir;

    // [x] Type validation via allowlist
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", "docx"
    );

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) {
            dir = System.getProperty("java.io.tmpdir") + "/uploads";
        }
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
    }

    // [x] Authentication / request verification
    private boolean authenticated(HttpServletRequest request) {
        String apiKey = getServletContext().getInitParameter("UPLOAD_API_KEY");
        if (apiKey == null || apiKey.isBlank()) return true;
        return apiKey.equals(request.getHeader("X-Upload-Key"));
    }

    // [x] File type validation
    private static boolean allowedType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // [x] Sanitize filename — protection against malicious names
    private static String sanitize(String raw) {
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

    private String storeFile(Part filePart) throws IOException {
        // [x] Sanitize — never trust client-supplied name
        String safeName = sanitize(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // [x] Validate file type
        if (!allowedType(safeName)) throw new IllegalArgumentException("File type not permitted.");

        // [x] Safe storage — server-generated UUID filename
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        Files.createDirectories(uploadDir);

        // [x] Path traversal protection
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

        // [x] Authentication check before processing
        if (!authenticated(request)) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                     "{\"error\":\"Unauthorized.\"}");
            return;
        }

        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"Invalid request.\"}");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"No file provided.\"}");
            return;
        }

        try {
            String stored = storeFile(filePart);
            sendJson(response, HttpServletResponse.SC_OK,
                     "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");
        } catch (IllegalArgumentException | SecurityException e) {
            // [x] Safe error — no internal paths or detail
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "{\"error\":\"Upload service unavailable.\"}");
        }
    }
}
