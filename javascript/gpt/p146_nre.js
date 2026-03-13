// JavaScript (Node.js + Express) example with server-side session auth + parameterized SQL + transaction + fixed redirect

import express from "express";
import session from "express-session";
import sqlite3 from "sqlite3";
import { open } from "sqlite";

const app = express();
app.use(express.urlencoded({ extended: false }));
app.use(
  session({
    secret: "change-me",
    resave: false,
    saveUninitialized: false,
    cookie: { httpOnly: true, sameSite: "lax" },
  })
);

const TICKER_RE = /^[A-Z0-9.\-]{1,8}$/;
const MAX_QTY = 100000;

const dbPromise = open({ filename: "app.db", driver: sqlite3.Database });

app.post("/buy", async (req, res) => {
  const username = req.session.user_id; // server-side session/auth context
  if (!username) return res.sendStatus(401);

  const stock_name = String(req.body.stock_name ?? "").trim().toUpperCase();
  const qtyStr = String(req.body.stock_quantity ?? "").trim();

  if (!TICKER_RE.test(stock_name)) return res.status(400).send("Invalid stock_name");

  const stock_quantity = Number.parseInt(qtyStr, 10);
  if (!Number.isInteger(stock_quantity) || stock_quantity < 1 || stock_quantity > MAX_QTY) {
    return res.status(400).send("Invalid stock_quantity");
  }

  const db = await dbPromise;

  await db.exec("BEGIN");
  try {
    await db.run(
      "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES (?, ?, ?)",
      [username, stock_name, stock_quantity]
    );
    await db.exec("COMMIT");
  } catch (e) {
    await db.exec("ROLLBACK");
    throw e;
  }

  // Fixed internal redirect only
  res.redirect(303, "/profile");
});

app.listen(3000);