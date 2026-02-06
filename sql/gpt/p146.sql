-- Example schema
CREATE TABLE IF NOT EXISTS buy_orders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,     -- use SERIAL for Postgres
  username TEXT NOT NULL,
  stock_name TEXT NOT NULL,
  stock_quantity INTEGER NOT NULL CHECK (stock_quantity > 0),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert buy order (bind params from app code)
INSERT INTO buy_orders (username, stock_name, stock_quantity)
VALUES (:username, :stock_name, :stock_quantity);
