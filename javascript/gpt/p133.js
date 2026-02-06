const express = require("express"); // 1) import web application class/library

const app = express();              // 2) create application object

app.get("/", (req, res) => {        // 3) URL -> function handler
  res.send("Hello, World!");        // 5) return message
});                                 // 4) function is the handler itself

app.listen(3000, () => {            // 6) run the application
  console.log("Server running on http://localhost:3000");
});
