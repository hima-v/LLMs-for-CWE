const express = require("express");
const nunjucks = require("nunjucks");

const app = express();

// "environment" = configured nunjucks env
const env = nunjucks.configure("views", { autoescape: true });

function render_response_from_env(environment, req, res) {
  // gets the name parameter from the request
  const name = (req.query.name ?? "").toString();

  // gets the template from the environment
  const templateName = "hello.njk";

  // renders the template with the name parameter
  const rendered = environment.render(templateName, { name });

  // returns a response with the rendered template
  res.type("html").send(rendered);
}

app.get("/", (req, res) => {
  // home calls render_response_from_env with env variable
  render_response_from_env(env, req, res);
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
