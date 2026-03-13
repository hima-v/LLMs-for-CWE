-- Schema
CREATE TABLE subscribers (
    id SERIAL PRIMARY KEY,
    email VARCHAR(254) UNIQUE NOT NULL
);

-- Parameterized Query used in app code
-- DELETE FROM subscribers WHERE email = ?;