// SecureUploadServer.java
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class SecureUploadServer {
    private static final Path BASE_DIR = Paths.get("").toAbsolutePath().normalize();
    private static final Path UPLOAD_DIR = BASE_DIR.resolve("uploads").normalize();
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt");
    private static final Pattern SAFE_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    public static void main(String[] args) throws Exception {
        ensureUploadDir();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/upload", SecureUploadServer::handleUpload);
        server.setExecutor(null);
        server.start();

        System.out.println("Server running on http://127.0.0.1:8080/upload");
    }

    private static void ensureUploadDir() throws IOException {
        Files.createDirectories(UPLOAD_DIR);
    }

    private static boolean file_is_safe_type(String filename) {
        String ext = getExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private static String sanitizeFilename(String filename) {
        String base = Paths.get(filename).getFileName().toString();
        String ext = getExtension(base);
        String stem = base.substring(0, base.length() - ext.length());
        stem = SAFE_CHARS.matcher(stem).replaceAll("_").replaceAll("^[._-]+|[._-]+$", "");

        if (stem.isBlank() || ext.isBlank()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        return UUID.randomUUID() + "_" + stem + ext.toLowerCase();
    }

    private static Path safeDestination(String filename) {
        Path dest = UPLOAD_DIR.resolve(filename).normalize().toAbsolutePath();
        if (!dest.startsWith(UPLOAD_DIR.toAbsolutePath())) {
            throw new IllegalArgumentException("Unsafe path");
        }
        return dest;
    }

    private static String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : "";
    }

    private static void upload_file(String filename, InputStream dataStream, long contentLength) throws IOException {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Missing filename");
        }

        if (contentLength < 0 || contentLength > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Invalid file size");
        }

        if (!file_is_safe_type(filename)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        ensureUploadDir();

        String safeName = sanitizeFilename(filename);
        Path dest = safeDestination(safeName);

        try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW)) {
            byte[] buffer = new byte[8192];
            long total = 0;
            int read;

            while ((read = dataStream.read(buffer)) != -1) {
                total += read;
                if (total > MAX_FILE_SIZE) {
                    throw new IllegalArgumentException("Invalid file size");
                }
                out.write(buffer, 0, read);
            }
        }
    }

    private static void handleUpload(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            Headers headers = exchange.getRequestHeaders();
            String filename = headers.getFirst("X-Filename");
            String contentLengthHeader = headers.getFirst("Content-Length");

            long contentLength = -1;
            if (contentLengthHeader != null) {
                try {
                    contentLength = Long.parseLong(contentLengthHeader);
                } catch (NumberFormatException ignored) {
                    sendJson(exchange, 400, "{\"error\":\"Invalid upload request\"}");
                    return;
                }
            }

            upload_file(filename, exchange.getRequestBody(), contentLength);
            sendJson(exchange, 201, "{\"message\":\"Upload successful\"}");

        } catch (IllegalArgumentException ex) {
            sendJson(exchange, 400, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
        } catch (Exception ex) {
            sendJson(exchange, 500, "{\"error\":\"Upload failed\"}");
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}