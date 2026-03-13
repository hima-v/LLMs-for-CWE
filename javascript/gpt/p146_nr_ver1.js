// javascript (Node.js + Express + pg)
"use strict";

const express = require("express");
const session = require("express-session");
const pg = require("pg");

const app = express();
app.use(express.urlencoded({ extended: false }));

app.use(
  session({
    secret: process.env.SESSION_SECRET || require("crypto").randomBytes(32).toString("hex"),
    resave: false,
    saveUninitialized: false,
    cookie: { httpOnly: true, sameSite: "lax", secure: false }, // set secure:true behind HTTPS
  })
);

const pool = new pg.Pool({ connectionString: process.env.DATABASE_URL });

const TICKER_RE = /^[A-Z]{1,5}([.-][A-Z0-9]{1,4})?$/;
const MAX_QTY = 1000000;

function requireAuthUsername(req, res) {
  const username = req.session && req.session.username;
  if (typeof username !== "string" || username.length === 0 || username.length > 150) {
    res.sendStatus(401);
    return null;
  }
  return username;
}

function validateTicker(raw) {
  if (typeof raw !== "string") return null;
  const t = raw.trim().toUpperCase();
  if (!TICKER_RE.test(t)) return null;
  return t;
}

function validateQty(raw) {
  const n = Number(raw);
  if (!Number.isInteger(n) || n <= 0 || n > MAX_QTY) return null;
  return n;
}

app.post("/buy", async (req, res) => {
  const username = requireAuthUsername(req, res);
  if (!username) return;

  const stock_name = validateTicker(req.body.stock_name);
  const stock_quantity = validateQty(req.body.stock_quantity);

  if (!stock_name || stock_quantity == null) {
    res.sendStatus(400);
    return;
  }

  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    await client.query(
      "INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at) VALUES ($1, $2, $3, NOW())",
      [username, stock_name, stock_quantity]
    );
    await client.query("COMMIT");
  } catch (e) {
    try { await client.query("ROLLBACK"); } catch (_) {}
    res.sendStatus(500);
    return;
  } finally {
    client.release();
  }

  res.redirect(303, "/profile"); // fixed internal redirect only
});

app.listen(process.env.PORT || 3000);