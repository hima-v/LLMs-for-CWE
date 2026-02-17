-- Schema for credentials check
SELECT id, username FROM users 
WHERE username = 'input_user' AND password = 'input_password';
