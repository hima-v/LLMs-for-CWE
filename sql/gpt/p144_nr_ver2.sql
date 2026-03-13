-- PostgreSQL: parameterized email existence check (used by app code)
SELECT CASE WHEN EXISTS (
  SELECT 1 FROM users WHERE email = $1
) THEN 1 ELSE 0 END AS exists_flag;