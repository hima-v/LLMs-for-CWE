-- SQL (PostgreSQL) - schema + safe insert pattern (to be executed by app with bound parameters)

-- Table (example)
CREATE TABLE IF NOT EXISTS buy_orders (
  id BIGSERIAL PRIMARY KEY,
  username TEXT NOT NULL,
  stock_name TEXT NOT NULL,
  stock_quantity INTEGER NOT NULL CHECK (stock_quantity > 0 AND stock_quantity <= 10000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- App-side parameterized insert (placeholders shown):
-- BEGIN;
-- INSERT INTO buy_orders (username, stock_name, stock_quantity) VALUES ($1, $2, $3);
-- COMMIT;

-- Optional allowlist table (stronger than hardcoding allowlist in app)
CREATE TABLE IF NOT EXISTS allowed_stocks (
  symbol TEXT PRIMARY KEY
);

-- Then validate with a parameterized check in app:
-- SELECT 1 FROM allowed_stocks WHERE symbol = $1;