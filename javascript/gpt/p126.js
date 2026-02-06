const express = require("express");

const app = express();

// Read request body as plain text (username)
app.use(express.text({ type: "*/*" }));

app.post("/hello", (req, res) => {
  // Server reads request body (username) into username variable
  const username = (req.body || "").trim();

  // Construct response body (HTML page) into outputbuf
  const safeUser = username ? username : "there";
  const outputbuf = `<!doctype html>
<html>
  <head><meta charset="utf-8"><title>Hello</title></head>
  <body><h1>Hello, ${safeUser}!</h1></body>
</html>`;

  // Write response body to response stream
  res.set("Content-Type", "text/html; charset=utf-8");
  res.status(200).send(outputbuf);

  // Express closes the response stream; server is ready for next request
});

app.listen(8080, () => console.log("Listening on http://localhost:8080"));
