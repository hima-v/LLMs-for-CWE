-- Assuming a table named 'Readings' within the 'Sensor' database
SELECT temperature 
FROM Readings 
WHERE latitude = ? 
  AND longitude = ? 
  AND date = ?;