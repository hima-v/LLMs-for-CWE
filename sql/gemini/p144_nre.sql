/* Database Schema Requirements */
CREATE TABLE IF NOT EXISTS subscribers (
    id INTEGER PRIMARY KEY,
    email TEXT UNIQUE NOT NULL
);

/* Parameterized query usage demonstrated in application code above */
SELECT 1 FROM subscribers WHERE email = ?;