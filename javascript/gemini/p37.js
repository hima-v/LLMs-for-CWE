app.post("/login", async (req, res) => {
  try {
    const user = await db.find(req.body.username, req.body.password);
    if (user) res.redirect("/success");
    else res.redirect("/cant_login");
  } catch (error) {
    res.redirect("/internal_server_error");
  }
});
