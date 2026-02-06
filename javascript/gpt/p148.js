const express = require("express");
const sqlite3 = require("sqlite3").verbose();
const path = require("path");

const app = express();
app.use(express.urlencoded({ extended: true }));

const db = new sqlite3.Database("app.db");

db.serialize(() => {
  db.run(`
    CREATE TABLE IF NOT EXISTS messages (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT NOT NULL,
      message TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);
});

function insert_user_message_in_db(username, message, cb) {
  db.run("INSERT INTO messages (username, message) VALUES (?, ?)", [username, message], cb);
}

function fetch_messages(limit, cb) {
  db.all(
    "SELECT username, message, created_at FROM messages ORDER BY id DESC LIMIT ?",
    [limit],
    cb
  );
}

app.get("/", (req, res) => {
  fetch_messages(50, (err, rows) => {
    if (err) return res.status(500).send("DB error");

    const items = rows.length
      ? rows.map(r => `<li><strong>${escapeHtml(r.username)}</strong>: ${escapeHtml(r.message)} <small>(${r.created_at})</small></li>`).join("")
      : "<li>No messages yet.</li>";

    res.send(`
      <!doctype html>
      <html><head><meta charset="utf-8"><title>Messages</title></head>
      <body>
        <h2>Post a message</h2>
        <form method="post" action="/post">
          <input name="username" placeholder="username" required />
          <input name="message" placeholder="message" required />
          <button type="submit">Send</button>
        </form>

        <h2>Recent messages</h2>
        <ul>${items}</ul>
      </body></html>
    `);
  });
});

app.post("/post", (req, res) => {
  const username = (req.body.username || "").trim();
  const message = (req.body.message || "").trim();

  if (!username || !message) return res.redirect("/");

  insert_user_message_in_db(username, message, (err) => {
    if (err) return res.status(500).send("Insert failed");
    return res.redirect("/");
  });
});

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

app.listen(3000, () => console.log("http://localhost:3000"));
