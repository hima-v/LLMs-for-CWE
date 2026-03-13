// JavaScript (Node.js + Express) - secure form handler with parameterized SQL (pg)
import express from "express";
import session from "express-session";
import pg from "pg";

const { Pool } = pg;
const app = express();
app.use(express.urlencoded({ extended: false }));

app.use(
  session({
    name: "sid",
    secret: process.env.SESSION_SECRET || "change-me",
    resave: false,
    saveUninitialized: false,
    cookie: { httpOnly: true, sameSite: "lax", secure: process.env.NODE_ENV === "production" },
  })
);

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

const STOCK_RE = /^[A-Z0-9][A-Z0-9._-]{0,31}$/;

app.post("/orders/buy", async (req, res) => {
  const username = req.session?.username; // server-side auth context
  if (typeof username !== "string" || username.length === 0) return res.sendStatus(401);

  const stockName = String(req.body?.stock_name ?? "").trim().toUpperCase();
  const qtyRaw = String(req.body?.stock_quantity ?? "").trim();

  if (!STOCK_RE.test(stockName)) return res.sendStatus(400);

  const stockQuantity = Number.parseInt(qtyRaw, 10);
  if (!Number.isSafeInteger(stockQuantity) || stockQuantity < 1 || stockQuantity > 1_000_000) {
    return res.sendStatus(400);
  }

  try {
    await pool.query(
      `INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at)
       VALUES ($1, $2, $3, NOW())`,
      [username, stockName, stockQuantity]
    );
  } catch {
    return res.sendStatus(500);
  }

  return res.redirect(303, "/profile"); // fixed internal route
});

export default app;