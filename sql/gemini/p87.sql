SELECT temperature 
FROM weather_readings 
WHERE latitude = ? 
  AND longitude = ? 
  AND reading_date = ?;