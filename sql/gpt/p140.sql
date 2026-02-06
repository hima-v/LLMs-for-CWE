SELECT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'given_username'
);
