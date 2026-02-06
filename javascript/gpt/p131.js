const express = require("express");
const nunjucks = require("nunjucks");

const app = express();

// Example "environment" (nunjucks) with an in-memory template
const env = new nunjucks.Environment(new nunjucks.Loader());
env.addTemplate("home.html", "Hello, {{ name }}!");

function render_response_from_env(environment, req, res) {
  // get the name parameter from the user request
  const name = (req.query.name ?? "World").toString();

  // get the template from the environment
  const templateName = "home.html";

  // render the template with the name parameter
  const rendered = environment.render(templateName, { name });

  // return a response with the rendered template
  res.status(200).type("html").send(rendered);
}

app.get("/", (req, res) => {
  // home calls render_response_from_env with env
  return render_response_from_env(env, req, res);
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
