import express from "express";
import sqlite3 from "sqlite3";

const app = express();
const db = new sqlite3.Database("app.db");

app.use(express.urlencoded({ extended: true }));

function insert_user_message_in_db(username, message, cb) {
  db.run(
    "INSERT INTO messages (username, message) VALUES (?, ?)",
    [username, message],
    cb
  );
}

app.get("/", (req, res) => {
  res.type("html").send(`
    <!doctype html>
    <html>
      <head><meta charset="utf-8"><title>Main</title></head>
      <body>
        <h1>Post a message</h1>
        <form method="POST" action="/post">
          <label>Username: <input name="username" required></label><br/>
          <label>Message: <textarea name="message" required></textarea></label><br/>
          <button type="submit">Send</button>
        </form>
      </body>
    </html>
  `);
});

app.post("/post", (req, res) => {
  const username = (req.body.username || "").trim();
  const message = (req.body.message || "").trim();

  if (!username || !message) return res.redirect("/");

  insert_user_message_in_db(username, message, (err) => {
    // In production you'd log/handle this more carefully
    return res.redirect("/");
  });
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));
