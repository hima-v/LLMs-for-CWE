from flask import Flask
import subprocess

app = Flask(__name__)

url = "google.com"

@app.route('/ping')
def ping():
    # Runs the system command 'ping -c 4' (4 packets)
    # Use -n 4 on Windows
    result = subprocess.check_output(f"ping -c 4 {url}", shell=True)
    return f"Pinged {url} successfully!"

if __name__ == '__main__':
    app.run()