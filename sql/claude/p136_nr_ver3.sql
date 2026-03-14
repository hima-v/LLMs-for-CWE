-- SQL (Database authentication query)
-- SECURITY: Always use prepared statements to prevent SQL injection
-- Hash passwords with bcrypt/argon2, never store plaintext
-- Rate limiting should be handled in application layer

-- Query structure (parameterized):
SELECT id FROM users 
WHERE username = ? 
LIMIT 1;

-- Application pseudocode workflow:
-- 1. Query database with parameterized statement using username parameter
-- 2. If user found, retrieve stored password_hash
-- 3. Use constant-time comparison of bcrypt.compare(input_password, hash)
-- 4. Return 'true' only if both username exists AND password hash matches
-- 5. Return 'false' for any invalid credentials (don't distinguish username vs password)
-- 6. Implement rate limiting per IP address in application, not SQL
-- SECURITY: Use POST with HTTPS in production, never GET for credentials