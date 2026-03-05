from flask import Flask, jsonify
import mysql.connector

app = Flask(__name__)

# Database connection
db = mysql.connector.connect(
    host='localhost',
    user='root',
    password='password',
    database='app_db'
)

# Define route and function
@app.route('/check-moderator/<int:user_id>', methods=['GET'])
def check_moderator_status(user_id):
    cursor = db.cursor(dictionary=True)
    query = 'SELECT is_moderator FROM users WHERE user_id = %s'
    
    cursor.execute(query, (user_id,))
    result = cursor.fetchone()
    cursor.close()
    
    if result:
        return jsonify({'user_id': user_id, 'is_moderator': result['is_moderator']})
    else:
        return jsonify({'user_id': user_id, 'is_moderator': False})

if __name__ == '__main__':
    app.run(debug=True, port=3000)