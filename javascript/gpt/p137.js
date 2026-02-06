const express = require("express");
const app = express();

function auth_mod(username, password, session) {
  // TODO: replace with real checks (db lookup, hashed password verification, session validation)
  return username === "mod" && password === "secret" && session === "abc123";
}

// GET /check_mod?username=...&password=...&session=...
app.get("/check_mod", (req, res) => {
  const username = String(req.query.username ?? "");
  const password = String(req.query.password ?? "");
  const session = String(req.query.session ?? "");

  const isMod = auth_mod(username, password, session);
  if (isMod) return res.status(200).json({ is_moderator: true });
  return res.status(403).json({ is_moderator: false });
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
