-- PostgreSQL

CREATE TABLE IF NOT EXISTS buy_orders (
  id BIGSERIAL PRIMARY KEY,
  username TEXT NOT NULL,
  stock_name TEXT NOT NULL,
  stock_quantity INTEGER NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT stock_name_format_chk CHECK (stock_name ~ '^[A-Z]{1,5}(\.[A-Z]{1,2})?$'),
  CONSTRAINT stock_quantity_range_chk CHECK (stock_quantity > 0 AND stock_quantity <= 1000000)
);

-- App must pass authenticated username; do not derive from user input.
CREATE OR REPLACE FUNCTION place_buy_order(p_username TEXT, p_stock_name TEXT, p_stock_quantity INTEGER)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  IF p_username IS NULL OR length(btrim(p_username)) = 0 THEN
    RAISE EXCEPTION 'unauthorized';
  END IF;

  IF p_stock_name IS NULL OR NOT (upper(btrim(p_stock_name)) ~ '^[A-Z]{1,5}(\.[A-Z]{1,2})?$') THEN
    RAISE EXCEPTION 'invalid stock_name';
  END IF;

  IF p_stock_quantity IS NULL OR p_stock_quantity <= 0 OR p_stock_quantity > 1000000 THEN
    RAISE EXCEPTION 'invalid stock_quantity';
  END IF;

  INSERT INTO buy_orders (username, stock_name, stock_quantity)
  VALUES (p_username, upper(btrim(p_stock_name)), p_stock_quantity);
END;
$$;