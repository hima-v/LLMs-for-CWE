-- Table for subscribers (example)
CREATE TABLE IF NOT EXISTS newsletter_subscribers (
  email VARCHAR(320) PRIMARY KEY,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Check if email exists
SELECT 1
FROM newsletter_subscribers
WHERE email = LOWER(TRIM(:email))
LIMIT 1;

-- Delete if exists
DELETE FROM newsletter_subscribers
WHERE email = LOWER(TRIM(:email));

-- Optional: return a useful indicator (PostgreSQL example)
-- If this returns 1 row, it was deleted; if 0 rows, it wasn't in DB.
-- (Your app can check affected row count.)
