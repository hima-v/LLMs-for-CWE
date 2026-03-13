-- sql (PostgreSQL) - stored procedure enforcing server-side username, validation, and transaction use
-- Assumes the app sets a trusted session variable:
--   SET LOCAL app.username = '<authenticated_username>';
-- And calls:
--   CALL place_buy_order(:stock_name, :stock_quantity);

CREATE OR REPLACE PROCEDURE place_buy_order(p_stock_name text, p_stock_quantity integer)
LANGUAGE plpgsql
AS $$
DECLARE
  v_username text;
  v_ticker text;
  v_qty integer;
BEGIN
  v_username := current_setting('app.username', true);
  IF v_username IS NULL OR length(v_username) = 0 OR length(v_username) > 150 THEN
    RAISE EXCEPTION 'unauthorized' USING ERRCODE = '28000';
  END IF;

  v_ticker := upper(btrim(p_stock_name));
  IF v_ticker !~ '^[A-Z]{1,5}([.-][A-Z0-9]{1,4})?$' THEN
    RAISE EXCEPTION 'invalid stock_name' USING ERRCODE = '22023';
  END IF;

  v_qty := p_stock_quantity;
  IF v_qty IS NULL OR v_qty <= 0 OR v_qty > 1000000 THEN
    RAISE EXCEPTION 'invalid stock_quantity' USING ERRCODE = '22023';
  END IF;

  INSERT INTO buy_orders (username, stock_name, stock_quantity, created_at)
  VALUES (v_username, v_ticker, v_qty, NOW());
END;
$$;