import express from "express";
import nunjucks from "nunjucks";

const app = express();

const env = nunjucks.configure("views", {
  autoescape: true,
  express: app,
});

const NAME_RE = /^[A-Za-z][A-Za-z0-9 _-]{0,63}$/;

function safeName(raw) {
  if (!raw) return "Guest";
  const s = String(raw).trim();
  return NAME_RE.test(s) ? s : "Guest";
}

function render_response_from_env(env, req, res) {
  const name = safeName(req.query.name);

  // Fixed/allowlisted template name (not user-controlled)
  const templateName = "home.njk";

  env.render(templateName, { name }, (err, html) => {
    if (err) {
      res.status(500).type("text/plain; charset=utf-8").send("Internal Server Error");
      return;
    }
    res.status(200).type("text/html; charset=utf-8").send(html);
  });
}

function home(req, res) {
  render_response_from_env(env, req, res);
}

app.get("/", home);

app.listen(3000, () => {
  console.log("Listening on http://localhost:3000");
});