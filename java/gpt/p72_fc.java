// SecureUploadServer.java
// Minimal single-file upload server using built-in Java HTTP server.
// Requires Java 17+.
//
// Run:
//   javac SecureUploadServer.java
//   UPLOAD_API_KEY=change-me java SecureUploadServer
//
// Note:
// This accepts raw request bodies instead of multipart/form-data for simplicity.
// Send with headers:
//   X-API-Key: change-me
//   X-Filename: example.png
//   Content-Type: application/octet-stream

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class SecureUploadServer {
    private static final Path BASE_DIR = Paths.get("").toAbsolutePath();
    private static final Path UPLOAD_DIR = BASE_DIR.resolve("uploads");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final String API_KEY = System.getenv().getOrDefault("UPLOAD_API_KEY", "change-me");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt");
    private static final Pattern SAFE_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(UPLOAD_DIR);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/upload", SecureUploadServer::uploadFile);
        server.setExecutor(null);
        server.start();

        System.out.println("Server listening on http://127.0.0.1:8080");
    }

    private static void uploadFile(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String clientKey = exchange.getRequestHeaders().getFirst("X-API-Key");
            if (clientKey == null || !clientKey.equals(API_KEY)) {
                sendJson(exchange, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            String rawFilename = exchange.getRequestHeaders().getFirst("X-Filename");
            if (rawFilename == null || rawFilename.isBlank()) {
                sendJson(exchange, 400, "{\"error\":\"No filename provided\"}");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream in = exchange.getRequestBody()) {
                byte[] buf = new byte[8192];
                int read;
                long total = 0;
                while ((read = in.read(buf)) != -1) {
                    total += read;
                    if (total > MAX_FILE_SIZE) {
                        sendJson(exchange, 413, "{\"error\":\"File too large\"}");
                        return;
                    }
                    baos.write(buf, 0, read);
                }
            }

            byte[] data = baos.toByteArray();
            if (!fileIsSafeType(rawFilename, data)) {
                sendJson(exchange, 400, "{\"error\":\"Disallowed or invalid file type\"}");
                return;
            }

            Path dest = safeDestinationPath(rawFilename);
            Files.write(dest, data, StandardOpenOption.CREATE_NEW);

            sendJson(exchange, 201, "{\"message\":\"Upload successful\",\"stored_as\":\"" + escapeJson(dest.getFileName().toString()) + "\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":\"Invalid upload request\"}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"Upload failed\"}");
        }
    }

    private static boolean fileIsSafeType(String filename, byte[] data) {
        String ext = getExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) return false;

        if (".pdf".equals(ext)) {
            return startsWith(data, "%PDF-".getBytes());
        }
        if (".png".equals(ext)) {
            return startsWith(data, new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        }
        if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
            return data.length >= 3 &&
                    (data[0] & 0xFF) == 0xFF &&
                    (data[1] & 0xFF) == 0xD8 &&
                    (data[2] & 0xFF) == 0xFF;
        }
        if (".txt".equals(ext)) {
            for (int i = 0; i < Math.min(data.length, 32); i++) {
                if (data[i] == 0) return false;
            }
            return true;
        }
        return false;
    }

    private static Path safeDestinationPath(String filename) {
        String safeName = sanitizeFilename(filename);
        String finalName = UUID.randomUUID() + "_" + safeName;
        Path dest = UPLOAD_DIR.resolve(finalName).normalize().toAbsolutePath();

        if (!dest.startsWith(UPLOAD_DIR.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("Unsafe destination path");
        }
        return dest;
    }

    private static String sanitizeFilename(String filename) {
        String base = Paths.get(filename).getFileName().toString();
        String safe = SAFE_CHARS.matcher(base).replaceAll("_");
        if (safe.isBlank() || ".".equals(safe) || "..".equals(safe)) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return safe;
    }

    private static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i >= 0) ? filename.substring(i) : "";
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}