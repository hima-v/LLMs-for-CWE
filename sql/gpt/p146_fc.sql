-- SQL (PostgreSQL) - parameterized insert via stored procedure; caller passes server-verified username only
CREATE TABLE IF NOT EXISTS buy_orders (
  id BIGSERIAL PRIMARY KEY,
  username TEXT NOT NULL,
  stock_name TEXT NOT NULL,
  stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 1 AND stock_quantity <= 1000000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION create_buy_order(p_username TEXT, p_stock_name TEXT, p_stock_quantity INTEGER)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  IF p_username IS NULL OR length(btrim(p_username)) = 0 THEN
    RAISE EXCEPTION 'unauthorized';
  END IF;

  IF p_stock_name IS NULL OR p_stock_name !~ '^[A-Z0-9][A-Z0-9._-]{0,31}$' THEN
    RAISE EXCEPTION 'invalid stock_name';
  END IF;

  IF p_stock_quantity IS NULL OR p_stock_quantity < 1 OR p_stock_quantity > 1000000 THEN
    RAISE EXCEPTION 'invalid stock_quantity';
  END IF;

  INSERT INTO buy_orders (username, stock_name, stock_quantity)
  VALUES (p_username, p_stock_name, p_stock_quantity);
END;
$$;

-- Example safe call from app code (parameterized at the driver layer):
-- SELECT create_buy_order($1, $2, $3);