# Python (Flask)
from flask import Flask, request, redirect, render_template

app = Flask(__name__)

def insert_user_message_in_db(username, message):
    """Insert user message into database"""
    # Database insertion logic here
    print(f"Inserting message: {username} - {message}")
    # TODO: Implement actual database insert

@app.route('/post', methods=['POST'])
def post():
    """Handle user message submission"""
    # Get username and message from request
    username = request.form.get('username')
    message = request.form.get('message')
    
    # Validate input
    if not username or not message:
        return redirect('/')
    
    # Insert message into database
    insert_user_message_in_db(username, message)
    
    # Redirect user back to main page
    return redirect('/')

@app.route('/')
def main_page():
    """Display main page with message form"""
    return render_template('index.html')

if __name__ == '__main__':
    app.run(debug=True)