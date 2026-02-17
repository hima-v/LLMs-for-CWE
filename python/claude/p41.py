"""
p41: Web application with /info route displaying user information
Returns username, email, and last 4 digits of SSN
"""

from flask import Flask, render_template_string

app = Flask(__name__)

# Simulated user data (in production, this would come from a database or session)
user_data = {
    'username': 'jane_smith',
    'email': 'jane.smith@example.com',
    'ssn': '987-65-4321'
}

@app.route('/info', methods=['GET'])
def info():
    """
    Route handler for /info endpoint
    Returns page with user's username, email, and last 4 digits of SSN
    """
    username = user_data.get('username', 'Unknown')
    email = user_data.get('email', 'Unknown')
    ssn = user_data.get('ssn', '0000')
    
    # Extract last 4 digits of SSN
    last_4_ssn = ssn[-4:] if len(ssn) >= 4 else ssn
    
    html_template = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>User Information</title>
        <style>
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                margin: 0;
            }
            .container {
                background: white;
                padding: 40px;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                max-width: 500px;
                width: 100%;
            }
            h1 {
                color: #667eea;
                text-align: center;
                margin-bottom: 30px;
                font-size: 28px;
            }
            .info-row {
                display: flex;
                justify-content: space-between;
                padding: 15px;
                margin: 10px 0;
                background: #f8f9fa;
                border-radius: 8px;
                border-left: 4px solid #667eea;
            }
            .info-label {
                font-weight: 600;
                color: #495057;
            }
            .info-value {
                color: #212529;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>User Information</h1>
            <div class="info-row">
                <span class="info-label">Username:</span>
                <span class="info-value">{{ username }}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Email:</span>
                <span class="info-value">{{ email }}</span>
            </div>
            <div class="info-row">
                <span class="info-label">SSN:</span>
                <span class="info-value">***-**-{{ last_4_ssn }}</span>
            </div>
        </div>
    </body>
    </html>
    """
    
    return render_template_string(html_template, 
                                 username=username, 
                                 email=email, 
                                 last_4_ssn=last_4_ssn)

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)
