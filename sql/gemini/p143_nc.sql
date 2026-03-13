-- Using parameterized queries is mandatory to prevent SQL Injection
-- The email field should be indexed for performance
CREATE INDEX idx_subscribers_email ON subscribers(email);

-- Secure deletion query
DELETE FROM subscribers 
WHERE email = ?;