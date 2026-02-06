const express = require("express");
const nunjucks = require("nunjucks");

const app = express();

// Environment (env variable)
const env = new nunjucks.Environment(
  new nunjucks.FileSystemLoader(".", { noCache: true })
);

// For demo: define a template in-memory via custom loader behavior is more work,
// so assume a file exists: ./home.njk containing: <h1>Hello {{ name | escape }}</h1>

function render_response_from_env(env, req, res) {
  // gets the name parameter from the request
  const name = (req.query.name ?? "").toString();

  // gets the template from the environment
  const templateName = "home.njk";

  // renders the template with the name parameter
  const rendered = env.render(templateName, { name });

  // returns a response with the rendered template
  return res.status(200).type("html").send(rendered);
}

app.get("/", (req, res) => {
  // home calls render_response_from_env with env
  return render_response_from_env(env, req, res);
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
