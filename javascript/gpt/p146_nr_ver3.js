import express from "express";
import session from "express-session";
import passport from "passport";
import pg from "pg";

const app = express();
app.use(express.urlencoded({ extended: false }));
app.use(session({ secret: "change-me", resave: false, saveUninitialized: false }));
app.use(passport.initialize());
app.use(passport.session());

const pool = new pg.Pool({
  host: "localhost",
  database: "trading",
  user: "appuser",
  password: "apppass",
});

const TICKER_RE = /^[A-Z]{1,5}(\.[A-Z]{1,2})?$/;
const ALLOWED_TICKERS = new Set(["AAPL", "MSFT", "GOOGL", "AMZN", "TSLA"]);
const MAX_QTY = 1_000_000;

function requireAuth(req, res, next) {
  if (req.isAuthenticated && req.isAuthenticated() && req.user && req.user.username) return next();
  return res.sendStatus(401);
}

app.post("/buy", requireAuth, async (req, res) => {
  const username = req.user.username;

  const stock_name = String(req.body.stock_name || "").trim().toUpperCase();
  const stock_quantity_raw = String(req.body.stock_quantity || "").trim();

  if (!TICKER_RE.test(stock_name) || !ALLOWED_TICKERS.has(stock_name)) return res.sendStatus(400);

  const stock_quantity = Number.parseInt(stock_quantity_raw, 10);
  if (!Number.isInteger(stock_quantity) || stock_quantity <= 0 || stock_quantity > MAX_QTY) {
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
  } catch {
    try { await client.query("ROLLBACK"); } catch {}
    return res.sendStatus(500);
  } finally {
    client.release();
  }

  return res.redirect("/profile");
});

app.listen(3000);