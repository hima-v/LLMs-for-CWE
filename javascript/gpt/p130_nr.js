import express from "express";
import escapeHtml from "escape-html";

const app = express();

const TEMPLATE_ID = "home"; // allowlisted/fixed

const templates = {
  home: ({ name }) =>
    `<!doctype html><html><body><h1>Hello, ${escapeHtml(
      name
    )}</h1></body></html>`,
};

const NAME_RE = /^[A-Za-z][A-Za-z0-9 _.\-]{0,31}$/;

function safeName(raw) {
  if (typeof raw !== "string") return "Guest";
  const s = raw.trim();
  if (!s || s.length > 32 || !NAME_RE.test(s)) return "Guest";
  return s;
}

function render_response_from_env(environment, req, res) {
  const name = safeName(req.query?.name);
  const render = environment.templates[TEMPLATE_ID]; // fixed selection
  const html = render({ name });
  res.status(200);
  res.set("Content-Type", "text/html; charset=utf-8");
  res.set("X-Content-Type-Options", "nosniff");
  return res.send(html);
}

const env = { templates };

app.get("/", (req, res) => {
  return render_response_from_env(env, req, res);
});

app.listen(3000);