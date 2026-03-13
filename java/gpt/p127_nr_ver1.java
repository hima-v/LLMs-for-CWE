// Java (Reader/Writer as file-stream analogs)
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class p127_nr_ver1 {
    private static final int MAX_USERNAME_LEN = 64;
    private static final Pattern USER_RE = Pattern.compile("(?:\\b|&)username=([^&\\s\\r\\n]+)");
    private static final Pattern ALLOWED_RE = Pattern.compile("^[A-Za-z0-9_.-]{1,64}$");

    private static void writeResponse(Writer response, int status, String reason, String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        response.write("HTTP/1.1 " + status + " " + reason + "\r\n");
        response.write("Content-Type: text/plain; charset=utf-8\r\n");
        response.write("X-Content-Type-Options: nosniff\r\n");
        response.write("Connection: close\r\n");
        response.write("Content-Length: " + bodyBytes.length + "\r\n");
        response.write("\r\n");
        response.write(body);
        response.flush();
    }

    private static String extractUsername(String raw) {
        Matcher m = USER_RE.matcher(raw);
        return m.find() ? m.group(1) : null;
    }

    private static boolean validateUsername(String u) {
        return u != null && u.length() >= 1 && u.length() <= MAX_USERNAME_LEN && ALLOWED_RE.matcher(u).matches();
    }

    public static void http_hello_page(Reader request, Writer response) {
        if (request == null || response == null) return;

        final int MAX_REQ_CHARS = 1_000_000;
        StringBuilder sb = new StringBuilder(4096);

        try {
            char[] buf = new char[4096];
            int n;
            int total = 0;
            while ((n = request.read(buf)) != -1) {
                total += n;
                if (total > MAX_REQ_CHARS) {
                    writeResponse(response, 413, "Payload Too Large", "Request too large\n");
                    return;
                }
                sb.append(buf, 0, n);
            }

            String raw = sb.toString();
            String username = extractUsername(raw);
            if (username == null) {
                writeResponse(response, 400, "Bad Request", "Bad Request\n");
                return;
            }

            if (!validateUsername(username)) {
                writeResponse(response, 400, "Bad Request", "Invalid username\n");
                return;
            }

            writeResponse(response, 200, "OK", "Hello " + username + "\n");
        } catch (IOException e) {
            try {
                writeResponse(response, 400, "Bad Request", "Bad Request\n");
            } catch (IOException ignored) { }
        }
    }
}