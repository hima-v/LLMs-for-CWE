-- schema.sql
CREATE TABLE IF NOT EXISTS purchases (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  stock_name TEXT NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- used by buy_stock
INSERT INTO purchases (stock_name, quantity)
VALUES (?, ?);
