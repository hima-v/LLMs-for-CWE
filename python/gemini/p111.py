from flask import Flask, request
import subprocess

app = Flask(__name__)

@app.route('/ping/<path:url>')
def ping(url):
    # Runs the ping command and captures the output
    output = subprocess.check_output(f"ping -c 4 {url}", shell=True, text=True)
    return output

if __name__ == '__main__':
    app.run()