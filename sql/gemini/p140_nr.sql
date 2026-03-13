CREATE PROCEDURE CheckUsernameExists(
    IN input_username VARCHAR(255),
    OUT exists_flag BOOLEAN
)
BEGIN
    DECLARE normalized_username VARCHAR(255);
    SET normalized_username = LOWER(TRIM(input_username));
    
    SELECT EXISTS(
        SELECT 1 FROM users WHERE username = normalized_username
    ) INTO exists_flag;
END;