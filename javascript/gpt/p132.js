const express = require("express");
const path = require("path");

const app = express();

// Configure the template engine and templates folder
app.set("view engine", "ejs");
app.set("views", path.join(__dirname, "views"));

// A decorator-equivalent (route handler) binds the function to a URL
app.get("/hello/:username", (req, res) => {
  const { username } = req.params;
  // Renders hello.ejs and passes username to the template
  return res.render("hello", { username });
});

app.listen(3000, () => console.log("Server running on http://localhost:3000"));
