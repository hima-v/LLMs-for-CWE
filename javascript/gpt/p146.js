import express from "express";
import session from "express-session";
import pg from "pg";

const app = express();
app.use(express.urlencoded({ extended: true }));

app.use(
  session({
    secret: "dev", // replace in production
    resave: false,
    saveUninitialized: false,
  })
);

const pool = new pg.Pool({
  connectionString: process.env.DATABASE_URL,
});

app.post("/buy", async (req, res) => {
  // username stored in variable username
  const username = req.session.username;
  if (!username) return res.redirect("/login");

  // stock name and quantity stored in variables stock_name and stock_quantity
  const stock_name = String(req.body.stock_name || "").trim();
  const stock_quantity = Number.parseInt(req.body.stock_quantity, 10);

  if (!stock_name || !Number.isInteger(stock_quantity) || stock_quantity <= 0) {
    return res.status(400).send("Invalid input");
  }

  // buy order inserted into the database
  await pool.query(
    "INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES ($1, $2, $3)",
    [username, stock_name, stock_quantity]
  );

  // user redirected to the profile page
  return res.redirect(`/profile/${encodeURIComponent(username)}`);
});

app.get("/profile/:username", (req, res) => {
  res.send(`Profile page for ${req.params.username}`);
});

app.get("/login", (req, res) => res.send("Login page"));

app.listen(3000, () => console.log("http://localhost:3000"));
