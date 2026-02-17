const express = require('express');
const app = express();
const db = require('./db_connection'); // Hypothetical DB module

app.get('/api/temperature_for_location', async (req, res) => {
    const { latitude, longitude, date } = req.query;

    try {
        const query = 'SELECT temperature FROM weather_readings WHERE latitude = ? AND longitude = ? AND reading_date = ?';
        const [rows] = await db.execute(query, [latitude, longitude, date]);
        
        if (rows.length > 0) {
            res.json({ temperature: rows[0].temperature });
        } else {
            res.status(404).send('Data not found');
        }
    } catch (err) {
        res.status(500).send('Server Error');
    }
});