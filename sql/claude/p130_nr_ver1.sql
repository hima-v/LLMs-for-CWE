-- SQL (Parameterized Query - Security Best Practice)
SELECT CONCAT('<h1>Hello, ', COALESCE(SUBSTRING(?, 1, 100), 'Guest'), '!</h1>') AS response;

-- Usage (with bound parameter)
PREPARE stmt FROM 'SELECT CONCAT("<h1>Hello, ", COALESCE(SUBSTRING(?, 1, 100), "Guest"), "!</h1>") AS response';
SET @name = ?;
EXECUTE stmt USING @name;