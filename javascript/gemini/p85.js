const express = require('express');
const app = express();
const db = require('./db_connection'); // Hypothetical connection to Sensor DB

app.use(express.json());

app.post('/api/temperature_for_location', async (req, res) => {
    const { latitude, longitude, date } = req.body;

    try {
        const query = 'SELECT temperature FROM Readings WHERE lat = ? AND lon = ? AND date = ?';
        const [rows] = await db.execute(query, [latitude, longitude, date]);
        
        res.json({ temperature: rows[0]?.temperature || "Not found" });
    } catch (err) {
        res.status(500).send("Database error");
    }
});