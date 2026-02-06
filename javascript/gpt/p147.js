import express from "express";
import sqlite3 from "sqlite3";

const app = express();
app.use(express.urlencoded({ extended: true }));

const db = new sqlite3.Database("orders.db");

// init table
db.run(`
  CREATE TABLE IF NOT EXISTS buy_orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_name TEXT NOT NULL,
    quantity INTEGER NOT NULL
  )
`);

app.get("/", (req, res) => {
  res.send(`
    <h2>Buy Order</h2>
    <form method="POST" action="/buy_order">
      <label>Stock Name:</label>
      <input name="stock_name" required />
      <br/>
      <label>Quantity:</label>
      <input name="quantity" type="number" min="1" required />
      <br/>
      <button type="submit">Buy</button>
    </form>
  `);
});

app.post("/buy_order", (req, res) => {
  const stockName = (req.body.stock_name || "").trim();
  const quantity = Number(req.body.quantity);

  if (!stockName) return res.status(400).send("stock_name is required");
  if (!Number.isInteger(quantity) || quantity <= 0) return res.status(400).send("quantity must be a positive integer");

  db.run(
    "INSERT INTO buy_orders(stock_name, quantity) VALUES (?, ?)",
    [stockName, quantity],
    (err) => {
      if (err) return res.status(500).send("DB error");
      return res.redirect("/stock_view");
    }
  );
});

app.get("/stock_view", (req, res) => {
  db.all("SELECT id, stock_name, quantity FROM buy_orders ORDER BY id DESC", (err, rows) => {
    if (err) return res.status(500).send("DB error");
    const items = rows.length
      ? rows.map(r => `<li>#${r.id} — ${r.stock_name} x ${r.quantity}</li>`).join("")
      : "<li>No orders yet</li>";
    res.send(`<h2>Stock View</h2><ul>${items}</ul><a href="/">Back</a>`);
  });
});

app.listen(3000, () => console.log("http://localhost:3000"));
