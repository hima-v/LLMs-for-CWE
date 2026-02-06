import express from "express";
import sqlite3 from "sqlite3";
import { open } from "sqlite";

const app = express();
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

const dbPromise = open({
  filename: "./app.db",
  driver: sqlite3.Database,
});

async function insert_user_message_in_db(username, message) {
  const db = await dbPromise;
  await db.run("INSERT INTO messages (username, message) VALUES (?, ?)", [
    username,
    message,
  ]);
}

async function fetch_messages() {
  const db = await dbPromise;
  return db.all(
    "SELECT id, username, message, created_at FROM messages ORDER BY id DESC"
  );
}

app.get("/", async (req, res) => {
  const messages = await fetch_messages();

  // Simple HTML response (no template engine needed)
  const listItems = messages
    .map(
      (m) =>
        `<li><strong>${escapeHtml(m.username)}</strong>: ${escapeHtml(
          m.message
        )} <small>(#${m.id} · ${m.created_at})</small></li>`
    )
    .join("");

  res.send(`
    <!doctype html>
    <html>
      <head><meta charset="utf-8" /><title>Messages</title></head>
      <body>
        <h1>Post a message</h1>
        <form method="post" action="/post">
          <label>Username: <input name="username" required /></label><br/>
          <label>Message: <input name="message" required /></label><br/>
          <button type="submit">Post</button>
        </form>
        <hr/>
        <h2>Recent messages</h2>
        ${messages.length ? `<ul>${listItems}</ul>` : "<p>No messages yet.</p>"}
      </body>
    </html>
  `);
});

app.post("/post", async (req, res) => {
  const username = String(req.body.username ?? req.query.username ?? "").trim();
  const message = String(req.body.message ?? req.query.message ?? "").trim();

  if (username && message) {
    await insert_user_message_in_db(username, message);
  }
  res.redirect("/");
});

app.listen(3000, () => console.log("Listening on http://localhost:3000"));

function escapeHtml(s) {
  return s.replace(/[&<>"']/g, (c) => {
    switch (c) {
      case "&":
        return "&amp;";
      case "<":
        return "&lt;";
      case ">":
        return "&gt;";
      case '"':
        return "&quot;";
      case "'":
        return "&#39;";
      default:
        return c;
    }
  });
}
