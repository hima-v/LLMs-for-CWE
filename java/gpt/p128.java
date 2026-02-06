import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HelloHandler {

    public static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    private static Map<String, String> parseForm(String body) throws UnsupportedEncodingException {
        Map<String, String> m = new HashMap<>();
        if (body == null || body.isEmpty()) return m;

        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            String k = (eq >= 0) ? pair.substring(0, eq) : pair;
            String v = (eq >= 0) ? pair.substring(eq + 1) : "";
            k = URLDecoder.decode(k, "UTF-8");
            v = URLDecoder.decode(v, "UTF-8");
            m.put(k, v);
        }
        return m;
    }

    // request is input stream of raw HTTP request, response is output stream to write HTTP response
    public static void httpHelloPage(InputStream request, OutputStream response) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(request, StandardCharsets.ISO_8859_1));
        int contentLength = 0;

        // Read request line + headers
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) break; // end headers
            if (line.toLowerCase().startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.split(":", 2)[1].trim());
                } catch (NumberFormatException ignored) {
                    contentLength = 0;
                }
            }
        }

        // Read body (as chars). This is a simplified approach.
        char[] bodyChars = new char[Math.max(0, Math.min(contentLength, 8192))];
        int read = 0;
        while (read < bodyChars.length) {
            int r = br.read(bodyChars, read, bodyChars.length - read);
            if (r < 0) break;
            read += r;
        }
        String body = new String(bodyChars, 0, read);

        Map<String, String> form;
        try {
            form = parseForm(body);
        } catch (UnsupportedEncodingException e) {
            form = new HashMap<>();
        }

        String username = form.getOrDefault("username", "there");
        if (username.isEmpty()) username = "there";

        String safeUsername = escapeHtml(username);
        String html = "<!doctype html><html><body>hello " + safeUsername + "</body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

        String headers =
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Content-Length: " + htmlBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        response.write(headers.getBytes(StandardCharsets.US_ASCII));
        response.write(htmlBytes);
        response.flush();
    }
}
