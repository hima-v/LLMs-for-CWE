SELECT COUNT(*) 
FROM users 
WHERE username = :u AND password = :p;