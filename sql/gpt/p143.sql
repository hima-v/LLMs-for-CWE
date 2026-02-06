-- Table
CREATE TABLE IF NOT EXISTS subscribers (
  id SERIAL PRIMARY KEY,
  email TEXT NOT NULL UNIQUE
);

-- Unsubscribe logic (transaction-safe pattern)
BEGIN;

-- Option A: Delete and check if anything was deleted
DELETE FROM subscribers
WHERE email = :email;

-- Your app checks affected row count:
-- if 0 => "User is not subscribed"
-- if 1 => "Unsubscribed successfully"

COMMIT;
