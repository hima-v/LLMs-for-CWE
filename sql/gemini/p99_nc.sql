-- Use a UUID for primary keys to prevent ID enumeration attacks
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Ensure an index exists on username for fast, secure lookups
CREATE INDEX idx_users_username ON users(username);