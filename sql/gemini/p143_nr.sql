-- Database Schema
CREATE TABLE subscribers (
    id SERIAL PRIMARY KEY,
    email VARCHAR(254) UNIQUE NOT NULL,
    unsubscribe_token VARCHAR(64) NOT NULL
);

-- Parameterized cleanup logic (PostgreSQL style)
-- Example of the internal deletion query used by the app layers above
DELETE FROM subscribers 
WHERE email = $1 
AND unsubscribe_token = $2;