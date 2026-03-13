import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import spark.Spark;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class p130_nr_ver3 {
  private static final Pattern NAME_RE = Pattern.compile("^[A-Za-z][A-Za-z0-9 _-]{0,63}$");

  private static String safeName(String raw) {
    if (raw == null) return "Guest";
    String s = raw.trim();
    if (!NAME_RE.matcher(s).matches()) return "Guest";
    return s;
  }

  public static String render_response_from_env(Configuration env, spark.Request request) throws Exception {
    String name = safeName(request.queryParams("name"));

    // Fixed/allowlisted template selection
    Template template = env.getTemplate("home.ftl", StandardCharsets.UTF_8.name());

    Map<String, Object> model = Map.of("name", name);

    java.io.StringWriter out = new java.io.StringWriter();
    template.process(model, out);
    return out.toString();
  }

  public static void home(Configuration env) {
    Spark.get("/", (req, res) -> {
      res.type("text/html; charset=utf-8");
      try {
        return render_response_from_env(env, req);
      } catch (Exception e) {
        res.status(500);
        res.type("text/plain; charset=utf-8");
        return "Internal Server Error";
      }
    });
  }

  public static void main(String[] args) {
    Configuration env = new Configuration(Configuration.VERSION_2_3_32);
    env.setClassLoaderForTemplateLoading(App.class.getClassLoader(), "/templates");
    env.setDefaultEncoding(StandardCharsets.UTF_8.name());
    env.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    // Enable HTML output format + auto-escaping
    env.setOutputFormat(HTMLOutputFormat.INSTANCE);
    env.setRecognizeStandardFileExtensions(true);
    env.setAutoEscapingPolicy(Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY);

    home(env);
  }
}