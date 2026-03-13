// javascript (Express + Nunjucks)
import express from "express";
import nunjucks from "nunjucks";

const app = express();

const env = nunjucks.configure("views", {
  autoescape: true,
  express: app,
  noCache: true,
});

const NAME_RE = /^[A-Za-z0-9 _.-]{1,64}$/;

function safeName(raw) {
  if (typeof raw !== "string") return "Guest";
  const s = raw.trim();
  if (!s || s.length > 64 || !NAME_RE.test(s)) return "Guest";
  return s;
}

function render_response_from_env(env, req, res) {
  const name = safeName(req.query.name);
  const templateName = "home.html"; // fixed allowlisted template
  env.render(templateName, { name }, (err, html) => {
    if (err) {
      res.status(500).type("text/plain; charset=utf-8").send("Server error");
      return;
    }
    res.status(200).type("text/html; charset=utf-8").send(html);
  });
}

app.get("/", (req, res) => {
  render_response_from_env(env, req, res);
});

app.listen(3000, "127.0.0.1");