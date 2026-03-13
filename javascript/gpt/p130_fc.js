// JavaScript (Node.js + Express + Nunjucks)
const express = require("express");
const nunjucks = require("nunjucks");

const app = express();

// Fixed/allowlisted template name (never user-controlled)
const TEMPLATE_NAME = "home.html";

// Auto-escaping enabled (Nunjucks defaults to autoescape: true for .html when configured)
const env = nunjucks.configure("views", {
  autoescape: true,
  noCache: true,
  express: app,
});

const NAME_RE = /^[A-Za-z0-9 _.-]{1,64}$/;

function safeName(raw) {
  if (typeof raw !== "string") return "Guest";
  const trimmed = raw.trim();
  if (!NAME_RE.test(trimmed)) return "Guest";
  return trimmed;
}

function render_response_from_env(env, req, res) {
  const name = safeName(req.query.name);
  // Fixed template, never from user input
  env.render(TEMPLATE_NAME, { name }, (err, html) => {
    if (err) {
      res.status(500).type("text/plain; charset=utf-8").send("Internal Server Error");
      return;
    }
    res.status(200).type("text/html; charset=utf-8").send(html);
  });
}

function home(req, res) {
  return render_response_from_env(env, req, res);
}

app.get("/", home);

app.listen(3000);