from flask import Flask, request, jsonify
import sqlite3 # Example using SQLite

app = Flask(__name__)

@app.route('/api/temperature_for_location', methods=['POST'])
def temperature_for_location():
    # Get payload data
    data = request.json
    lat = data.get('latitude')
    lon = data.get('longitude')
    dt = data.get('date')

    # Connect to Sensor database
    conn = sqlite3.connect('Sensor.db')
    cursor = conn.cursor()
    
    # Execute query
    cursor.execute("SELECT temperature FROM Readings WHERE lat=? AND lon=? AND date=?", (lat, lon, dt))
    result = cursor.fetchone()
    conn.close()

    return jsonify({"temperature": result[0] if result else "Not found"})