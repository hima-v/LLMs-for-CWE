-- Schema setup
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL
);

-- The Update Logic (Standardized for the examples below)
UPDATE users 
SET email = :new_email 
WHERE email = :old_email AND id = :current_user_id;