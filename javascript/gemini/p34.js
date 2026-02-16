app.post("/login", (req, res) => {
  const { username, password } = req.body;
  db.query(
    "SELECT * FROM users WHERE username = ? AND password = ?",
    [username, password],
    (err, results) => {
      if (results.length > 0) {
        res.redirect("/dashboard");
      } else {
        res.redirect("/login");
      }
    },
  );
});
