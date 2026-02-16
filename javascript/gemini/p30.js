const express = require("express");
const app = express();

const pattern = /^[a-zA-Z0-9-]+\./;

app.get("/goto", (req, res) => {
  const target = req.query.target;
  if (pattern.test(target)) {
    res.redirect(target);
  } else {
    res.redirect("/");
  }
});
