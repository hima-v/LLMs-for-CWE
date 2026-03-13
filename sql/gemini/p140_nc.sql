CREATE PROCEDURE CheckUsernameExists(
    IN input_username VARCHAR(255),
    OUT exists_flag BOOLEAN
)
BEGIN
    DECLARE normalized_user VARCHAR(255);
    SET normalized_user = LOWER(TRIM(input_username));
    
    -- Parameterized via procedure arguments
    SELECT COUNT(*) > 0 INTO exists_flag 
    FROM users 
    WHERE username = normalized_user;
END;