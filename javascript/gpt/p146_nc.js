// JavaScript (Express) - secure buy order insert + fixed redirect
import express from "express";
import session from "express-session";
import helmet from "helmet";
import { Pool } from "pg";

const app = express();
app.use(helmet());
app.use(express.urlencoded({ extended: false }));
app.use(
  session({
    secret: "replace-with-strong-secret",
    resave: false,
    saveUninitialized: false,
    cookie: { httpOnly: true, sameSite: "lax", secure: false },
  })
);

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

const ALLOWED_STOCKS = new Set(["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"]);
const MAX_QTY = 10000;

function requireAuth(req, res, next) {
  // Username comes from server-side session (not client)
  if (!req.session || !req.session.username) return res.sendStatus(401);
  next();
}

function validateStockName(raw) {
  if (typeof raw !== "string") throw new Error("bad_request");
  const stock = raw.trim().toUpperCase();
  if (!ALLOWED_STOCKS.has(stock)) throw new Error("bad_request");
  return stock;
}

function validateStockQuantity(raw) {
  const qty = Number(raw);
  if (!Number.isInteger(qty) || qty <= 0 || qty > MAX_QTY) throw new Error("bad_request");
  return qty;
}

app.post("/buy", requireAuth, async (req, res) => {
  const username = req.session.username;

  let stock_name, stock_quantity;
  try {
    stock_name = validateStockName(req.body.stock_name);
    stock_quantity = validateStockQuantity(req.body.stock_quantity);
  } catch {
    return res.sendStatus(400);
  }

  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    await client.query(
      "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES ($1, $2, $3)",
      [username, stock_name, stock_quantity]
    );
    await client.query("COMMIT");
  } catch (e) {
    await client.query("ROLLBACK");
    return res.sendStatus(500);
  } finally {
    client.release();
  }

  // Fixed internal redirect (no open redirect)
  return res.redirect("/profile");
});

app.get("/profile", requireAuth, (req, res) => {
  res.send("Profile page");
});

app.listen(3000);