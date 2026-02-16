function login(username, password, res) {
  db.getUser(username, (user) => {
    if (user && user.password === password) {
      return res.redirect("/home");
    }
    return;
  });
}
