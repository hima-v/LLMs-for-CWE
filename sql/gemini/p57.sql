UPDATE users 
SET email = 'new_email@example.com' 
WHERE email = 'old_email@example.com' AND password = 'confirm_password';
