-- table
CREATE TABLE IF NOT EXISTS buy_orders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  stock_name TEXT NOT NULL,
  quantity INTEGER NOT NULL
);

-- insert (parameterized in code; shown here conceptually)
INSERT INTO buy_orders (stock_name, quantity) VALUES (?, ?);

-- stock_view query
SELECT id, stock_name, quantity
FROM buy_orders
ORDER BY id DESC;
