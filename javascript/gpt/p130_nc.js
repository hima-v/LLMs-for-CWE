import express from "express";
import nunjucks from "nunjucks";

const app = express();

// Fixed/allowlisted template (not user-chosen)
const TEMPLATE_NAME = "home.njk";

// Auto-escaping enabled for HTML templates (default true in Nunjucks)
const env = nunjucks.configure("views", {
  autoescape: true,
  express: app,
});

const NAME_RE = /^[A-Za-z][A-Za-z0-9 _.-]{0,49}$/;

function safeName(value) {
  if (typeof value !== "string") return "Guest";
  const v = value.trim();
  if (!v || v.length > 50 || !NAME_RE.test(v)) return "Guest";
  return v;
}

function render_response_from_env(environment, req, res) {
  const name = safeName(req.query.name);
  const rendered = environment.render(TEMPLATE_NAME, { name });
  res.type("html; charset=utf-8").send(rendered);
}

app.get("/", (req, res) => {
  render_response_from_env(env, req, res);
});

app.listen(3000);