import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class p130_nr_ver1 {
  private static final String TEMPLATE_ID = "home"; // allowlisted/fixed
  private static final Pattern NAME_RE = Pattern.compile("^[A-Za-z][A-Za-z0-9 _\\.\\-]{0,31}$");

  public interface Template {
    String render(Map<String, String> model);
  }

  public static final class Environment {
    private final Map<String, Template> templates;
    public Environment(Map<String, Template> templates) { this.templates = templates; }
    public Template getTemplate(String id) { return templates.get(id); }
  }

  private static String escapeHtml(String s) {
    StringBuilder out = new StringBuilder(s.length() + 16);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '&': out.append("&amp;"); break;
        case '<': out.append("&lt;"); break;
        case '>': out.append("&gt;"); break;
        case '"': out.append("&quot;"); break;
        case '\'': out.append("&#x27;"); break;
        default: out.append(c);
      }
    }
    return out.toString();
  }

  private static String safeName(String raw) {
    if (raw == null) return "Guest";
    String s = raw.trim();
    if (s.isEmpty() || s.length() > 32 || !NAME_RE.matcher(s).matches()) return "Guest";
    return s;
  }

  public static void render_response_from_env(Environment env, HttpServletRequest req, HttpServletResponse resp)
      throws java.io.IOException {
    String name = safeName(req.getParameter("name"));
    Template tpl = env.getTemplate(TEMPLATE_ID); // fixed selection
    String html = tpl.render(Map.of("name", escapeHtml(name))); // output-encoded
    byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

    resp.setStatus(200);
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("text/html; charset=utf-8");
    resp.setHeader("X-Content-Type-Options", "nosniff");
    resp.setContentLength(bytes.length);
    resp.getOutputStream().write(bytes);
  }

  public static void home(Environment env, HttpServletRequest req, HttpServletResponse resp)
      throws java.io.IOException {
    render_response_from_env(env, req, resp);
  }

  public static Environment env() {
    return new Environment(
        Map.of(
            "home",
            model -> "<!doctype html><html><body><h1>Hello, " + model.getOrDefault("name", "Guest") + "</h1></body></html>"
        )
    );
  }
}
