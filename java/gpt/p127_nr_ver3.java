import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class p127_nr_ver3 {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{1,64}$");

    private static String readUsername(BufferedReader br) throws IOException {
        if (br == null) return null;
        for (int i = 0; i < 200; i++) {
            String line = br.readLine();
            if (line == null) break;
            if (line.isEmpty()) break;
            if (line.startsWith("username=")) return line.substring(9).trim();
            if (line.regionMatches(true, 0, "Username:", 0, 9)) return line.substring(9).trim();
        }
        return null;
    }

    public static void http_hello_page(InputStream request, OutputStream response) throws IOException {
        if (response == null) return;

        BufferedReader br = new BufferedReader(new InputStreamReader(request, StandardCharsets.UTF_8));
        String raw = readUsername(br);
        String username = (raw == null) ? "" : raw.trim();
        boolean ok = USERNAME.matcher(username).matches();

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response, StandardCharsets.UTF_8));
        out.write(ok ? "HTTP/1.1 200 OK\r\n" : "HTTP/1.1 400 Bad Request\r\n");
        out.write("Content-Type: text/plain; charset=utf-8\r\n");
        out.write("X-Content-Type-Options: nosniff\r\n");
        out.write("Connection: close\r\n");
        out.write("\r\n");
        out.write(ok ? "Hello " + username + "\n" : "Bad Request\n");
        out.flush();
    }
}