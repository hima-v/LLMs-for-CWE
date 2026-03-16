// SecureUploadServer.java
// Requires Java 17+ and Apache Commons FileUpload 2.x / Commons IO on classpath.
// Example run idea:
// javac -cp ".:commons-fileupload2-jakarta-servlet6-2.x.jar:commons-io-2.x.jar" SecureUploadServer.java
// java  -cp ".:commons-fileupload2-jakarta-servlet6-2.x.jar:commons-io-2.x.jar" SecureUploadServer

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.JakartaServletFileUpload;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class SecureUploadServer {
    private static final Path BASE_DIR = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    private static final Path UPLOAD_DIR = Paths.get(
            System.getenv().getOrDefault("UPLOAD_DIR", BASE_DIR.resolve("uploads").toString())
    ).toAbsolutePath().normalize();

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "application/pdf", "text/plain"
    );
    private static final Pattern SAFE_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
    private static final SecureRandom RNG = new SecureRandom();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(UPLOAD_DIR);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);

        server.createContext("/", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String html = """
                <!doctype html>
                <html>
                  <body>
                    <h2>Secure File Upload</h2>
                    <form method="post" action="/upload" enctype="multipart/form-data">
                      <input type="file" name="file" required />
                      <button type="submit">Upload</button>
                    </form>
                  </body>
                </html>
                """;
            send(exchange, 200, html, "text/html; charset=utf-8");
        });

        server.createContext("/upload", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "{\"ok\":false,\"error\":\"Method not allowed\"}", "application/json");
                return;
            }

            try {
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data")) {
                    send(exchange, 400, "{\"ok\":false,\"error\":\"Invalid upload request\"}", "application/json");
                    return;
                }

                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                if (requestBody.length > MAX_FILE_SIZE + 1024 * 1024) {
                    send(exchange, 400, "{\"ok\":false,\"error\":\"File too large\"}", "application/json");
                    return;
                }

                SimpleRequestContext ctx = new SimpleRequestContext(contentType, requestBody);
                DiskFileItemFactory factory = DiskFileItemFactory.builder().get();
                JakartaServletFileUpload<FileItem, DiskFileItemFactory> upload =
                        new JakartaServletFileUpload<>(factory);

                upload.setFileSizeMax(MAX_FILE_SIZE);
                List<FileItem> items = upload.parseRequest(ctx);

                FileItem uploadedFile = null;
                for (FileItem item : items) {
                    if (!item.isFormField() && "file".equals(item.getFieldName())) {
                        uploadedFile = item;
                        break;
                    }
                }

                String saved = uploadFile(uploadedFile);
                String json = "{\"ok\":true,\"file\":\"" + escapeJson(saved) + "\"}";
                send(exchange, 201, json, "application/json");
            } catch (IllegalArgumentException e) {
                send(exchange, 400, "{\"ok\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}", "application/json");
            } catch (FileUploadException e) {
                send(exchange, 400, "{\"ok\":false,\"error\":\"Invalid upload request\"}", "application/json");
            } catch (Exception e) {
                send(exchange, 500, "{\"ok\":false,\"error\":\"Upload failed\"}", "application/json");
            }
        });

        server.start();
        System.out.println("Server running on http://127.0.0.1:8080");
    }

    static String sanitizeFilename(String filename) {
        String base = filename == null ? "upload" : Paths.get(filename).getFileName().toString();
        base = SAFE_CHARS.matcher(base).replaceAll("_");
        if (base.isBlank()) base = "upload";
        return base.length() > 120 ? base.substring(0, 120) : base;
    }

    static boolean fileIsSafeType(String filename, String contentType, byte[] head) {
        String lower = filename.toLowerCase();
        int idx = lower.lastIndexOf('.');
        String ext = idx >= 0 ? lower.substring(idx) : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) return false;
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) return false;

        if (".png".equals(ext)) {
            return head.length >= 4 &&
                    (head[0] & 0xFF) == 0x89 &&
                    head[1] == 0x50 &&
                    head[2] == 0x4E &&
                    head[3] == 0x47;
        }

        if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
            return head.length >= 2 &&
                    (head[0] & 0xFF) == 0xFF &&
                    (head[1] & 0xFF) == 0xD8;
        }

        if (".pdf".equals(ext)) {
            return head.length >= 5 &&
                    head[0] == '%' &&
                    head[1] == 'P' &&
                    head[2] == 'D' &&
                    head[3] == 'F' &&
                    head[4] == '-';
        }

        if (".txt".equals(ext)) {
            for (byte b : head) {
                if (b == 0) return false;
            }
            return true;
        }

        return false;
    }

    static String uploadFile(FileItem item) throws Exception {
        if (item == null || item.getName() == null || item.getSize() <= 0) {
            throw new IllegalArgumentException("No file provided");
        }

        String safeName = sanitizeFilename(item.getName());
        byte[] data = item.getInputStream().readAllBytes();
        if (data.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large");
        }

        byte[] head = data.length > 8192 ? java.util.Arrays.copyOf(data, 8192) : data;
        if (!fileIsSafeType(safeName, item.getContentType(), head)) {
            throw new IllegalArgumentException("Unsupported or unsafe file type");
        }

        String ext = "";
        int idx = safeName.lastIndexOf('.');
        if (idx >= 0) ext = safeName.substring(idx).toLowerCase();

        String stem = idx >= 0 ? safeName.substring(0, idx) : safeName;
        if (stem.length() > 80) stem = stem.substring(0, 80);

        byte[] rand = new byte[16];
        RNG.nextBytes(rand);
        String finalName = stem + "_" + HexFormat.of().formatHex(rand) + ext;

        Path dest = UPLOAD_DIR.resolve(finalName).normalize();
        if (!dest.getParent().equals(UPLOAD_DIR)) {
            throw new IllegalArgumentException("Invalid destination path");
        }

        try (OutputStream out = Files.newOutputStream(
                dest,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        )) {
            out.write(data);
        } catch (FileAlreadyExistsException e) {
            throw new IllegalArgumentException("Could not store file");
        }

        return finalName;
    }

    static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static final class SimpleRequestContext implements org.apache.commons.fileupload2.core.RequestContext {
        private final String contentType;
        private final byte[] body;

        SimpleRequestContext(String contentType, byte[] body) {
            this.contentType = contentType;
            this.body = body;
        }

        @Override
        public String getCharacterEncoding() {
            return "UTF-8";
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public long getContentLength() {
            return body.length;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(body);
        }
    }
}