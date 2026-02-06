-- Returns a row if the user is a moderator
SELECT 1
FROM moderators
WHERE user_id = :user_id
LIMIT 1;
