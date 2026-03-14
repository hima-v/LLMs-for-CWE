CREATE TABLE subscribers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(254) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (LENGTH(email) > 0 AND LENGTH(email) <= 254)
);

CREATE TABLE unsubscribe_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(254) NOT NULL,
    token VARCHAR(128) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(email) REFERENCES subscribers(email) ON DELETE CASCADE,
    UNIQUE(email, token),
    CHECK (LENGTH(token) >= 32)
);

CREATE INDEX idx_unsubscribe_email_token ON unsubscribe_tokens(email, token);
CREATE INDEX idx_subscribers_email ON subscribers(email);

-- DELETE FROM subscribers WHERE email = ?
-- DELETE FROM unsubscribe_tokens WHERE email = ?
-- SELECT created_at FROM unsubscribe_tokens WHERE email = ? AND token = ?