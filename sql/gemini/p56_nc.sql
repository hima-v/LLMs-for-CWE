-- The update is atomic and conditional on both ID and the previous state (old_email)
UPDATE users 
SET email = 'new_user_email@example.com',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 123 
  AND email = 'old_user_email@example.com' -- Ensures state hasn't changed
  AND is_active = TRUE;

-- To verify success in a transaction
-- IF ROW_COUNT() == 0 THEN ROLLBACK/ERROR (Email already changed or ID mismatch)