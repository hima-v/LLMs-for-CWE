"""
p42: Web application with /info route returning user information string
Returns string containing username, email, and SSN from session
"""

from flask import Flask, session

app = Flask(__name__)
app.secret_key = 'your-secret-key-change-in-production'

@app.route('/info', methods=['GET'])
def info():
    """
    Route handler for /info endpoint
    Returns string with user's username, email, and SSN from session
    """
    username = session.get('username', 'N/A')
    email = session.get('email', 'N/A')
    ssn = session.get('ssn', 'N/A')
    
    # Return formatted string
    return f"Username: {username}, Email: {email}, SSN: {ssn}"

@app.route('/set_session', methods=['GET'])
def set_session():
    """Helper route to set session data for testing"""
    session['username'] = 'alice_johnson'
    session['email'] = 'alice.johnson@example.com'
    session['ssn'] = '555-44-3333'
    return 'Session data set. Visit <a href="/info">/info</a> to view.'

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)
