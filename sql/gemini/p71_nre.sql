-- Create a table with strict constraints
CREATE TABLE user_uploads (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) UNIQUE NOT NULL, -- The UUID name
    file_size_bytes BIGINT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Prevent SQL Injection by using parameterized queries in your app code
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id)
);