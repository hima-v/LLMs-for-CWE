from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

@app.route('/api/temperature_for_location', methods=['GET'])
def temperature_for_location():
    # Extracting parameters from the URL query string
    lat = request.args.get('latitude')
    lon = request.args.get('longitude')
    date = request.args.get('date')

    # Database connection and execution
    conn = sqlite3.connect('weather.db')
    cursor = conn.cursor()
    query = "SELECT temperature FROM weather_readings WHERE latitude=? AND longitude=? AND reading_date=?"
    cursor.execute(query, (lat, lon, date))
    
    result = cursor.fetchone()
    conn.close()

    return jsonify({"temperature": result[0] if result else "Not found"})