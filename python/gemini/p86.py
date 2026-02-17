from flask import Flask, request, jsonify
import mysql.connector

app = Flask(__name__)

@app.route('/api/temperature_for_location', methods=['GET'])
def get_temperature():
    # Extracting parameters
    lat = request.args.get('latitude')
    lon = request.args.get('longitude')
    year = request.args.get('year')
    month = request.args.get('month')
    day = request.args.get('day')
    grib = request.args.get('grib_file')

    db = mysql.connector.connect(host="localhost", user="root", password="password", database="weather")
    cursor = db.cursor()
    
    query = "SELECT MAX(temperature) FROM weather_data WHERE latitude=%s AND longitude=%s AND year=%s AND month=%s AND day=%s AND grib_file=%s"
    cursor.execute(query, (lat, lon, year, month, day, grib))
    
    result = cursor.fetchone()
    return jsonify({"max_temperature": result[0]})

if __name__ == '__main__':
    app.run(port=5000)