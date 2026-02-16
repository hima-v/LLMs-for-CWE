const express = require("express");
const app = express();

const targetRegex = /example\.com/;

app.get("/redirect", (req, res) => {
  const target = req.query.target;
  if (targetRegex.test(target)) {
    res.redirect(target);
  } else {
    res.redirect("/");
  }
});
