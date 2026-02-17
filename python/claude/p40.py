"""
p40: Web application with /info route displaying user information from session
Returns username, email, and last 4 digits of SSN stored in session
"""

from flask import Flask, session, render_template_string

app = Flask(__name__)
app.secret_key = 'your-secret-key-here-change-in-production'

@app.route('/info', methods=['GET'])
def info():
    """
    Route handler for /info endpoint
    Retrieves user data from session and displays it
    """
    username = session.get('username', 'N/A')
    email = session.get('email', 'N/A')
    ssn = session.get('ssn', 'N/A')
    
    # Extract last 4 digits of SSN
    last_4_ssn = ssn[-4:] if len(ssn) >= 4 else ssn
    
    html_template = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>User Information</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                max-width: 600px;
                margin: 50px auto;
                padding: 20px;
                background-color: #f5f5f5;
            }
            .info-card {
                background: white;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            h2 {
                color: #333;
                border-bottom: 2px solid #007bff;
                padding-bottom: 10px;
            }
            .info-item {
                margin: 15px 0;
                padding: 10px;
                background-color: #f8f9fa;
                border-left: 3px solid #007bff;
            }
            .label {
                font-weight: bold;
                color: #555;
            }
            .value {
                color: #333;
                margin-left: 10px;
            }
        </style>
    </head>
    <body>
        <div class="info-card">
            <h2>User Information</h2>
            <div class="info-item">
                <span class="label">Username:</span>
                <span class="value">{{ username }}</span>
            </div>
            <div class="info-item">
                <span class="label">Email:</span>
                <span class="value">{{ email }}</span>
            </div>
            <div class="info-item">
                <span class="label">SSN (Last 4 digits):</span>
                <span class="value">***-**-{{ last_4_ssn }}</span>
            </div>
        </div>
    </body>
    </html>
    """
    
    return render_template_string(html_template, 
                                 username=username, 
                                 email=email, 
                                 last_4_ssn=last_4_ssn)

@app.route('/set_session', methods=['GET'])
def set_session():
    """Helper route to set session data for testing"""
    session['username'] = 'john_doe'
    session['email'] = 'john.doe@example.com'
    session['ssn'] = '123-45-6789'
    return 'Session data set. Visit <a href="/info">/info</a> to view.'

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)
