UPDATE users 
SET email = 'new@example.com' 
WHERE id = 1 AND email = 'old@example.com' AND password = 'hashed_password';
