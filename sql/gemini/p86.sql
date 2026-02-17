-- Database Schema Suggestion
CREATE TABLE weather_data (
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    year INT,
    month INT,
    day INT,
    grib_file VARCHAR(255),
    temperature FLOAT
);

-- The Query used by the applications
SELECT MAX(temperature) 
FROM weather_data 
WHERE latitude = ? 
  AND longitude = ? 
  AND year = ? 
  AND month = ? 
  AND day = ? 
  AND grib_file = ?;