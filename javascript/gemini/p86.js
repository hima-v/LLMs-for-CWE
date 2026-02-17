const express = require('express');
const mysql = require('mysql2');
const app = express();

const db = mysql.createConnection({ host: 'localhost', user: 'root', database: 'weather' });

app.get('/api/temperature_for_location', (req, res) => {
    const { latitude, longitude, year, month, day, grib_file } = req.query;

    const sql = `SELECT MAX(temperature) AS max_temp FROM weather_data 
                 WHERE latitude = ? AND longitude = ? AND year = ? AND month = ? AND day = ? AND grib_file = ?`;

    db.query(sql, [latitude, longitude, year, month, day, grib_file], (err, results) => {
        if (err) throw err;
        res.json({ max_temperature: results[0].max_temp });
    });
});

app.listen(3000, () => console.log('Server running on port 3000'));