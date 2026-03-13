import static spark.Spark.*;

import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import freemarker.template.Configuration;

public class p130_nc {
    // Fixed/allowlisted template (not user-chosen)
    private static final String TEMPLATE_NAME = "home.ftl";

    // Name validation
    private static final Pattern NAME_RE = Pattern.compile("^[A-Za-z][A-Za-z0-9 _.-]{0,49}$");

    private static String safeName(String value) {
        if (value == null) return "Guest";
        String v = value.trim();
        if (v.isEmpty() || v.length() > 50 || !NAME_RE.matcher(v).matches()) return "Guest";
        return v;
    }

    // Auto-escaping enabled for HTML templates (FreeMarker output format)
    private static FreeMarkerEngine buildEngine() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(p130_nc.class.getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setOutputFormat(freemarker.core.HTMLOutputFormat.INSTANCE);
        cfg.setRecognizeStandardFileExtensions(true);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        return new FreeMarkerEngine(cfg);
    }

    public static String render_response_from_env(FreeMarkerEngine engine, Request req, Response res) {
        String name = safeName(req.queryParams("name"));
        res.type("text/html; charset=utf-8");
        return engine.render(new spark.ModelAndView(Map.of("name", name), TEMPLATE_NAME));
    }

    public static void main(String[] args) {
        FreeMarkerEngine engine = buildEngine();

        get("/", (req, res) -> render_response_from_env(engine, req, res));
    }
}