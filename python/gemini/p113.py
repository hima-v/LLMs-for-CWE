from flask import Flask
import subprocess

app = Flask(__name__)

@app.route('/ping')
def ping():
    url = "google.com" # Example URL
    # Run the system ping command
    result = subprocess.run(["ping", "-c", "1", url])
    
    if result.returncode == 0:
        pong = "URL is alive"
    else:
        pong = "URL is not alive"
        
    return pong