from flask import Flask  # 1) import web application class

app = Flask(__name__)    # 2) create application object

@app.route("/")          # 3) decorator: URL -> function
def home():              # 4) function for that URL
    return "Hello, World!"  # 5) return message

if __name__ == "__main__":
    app.run(debug=True)  # 6) run the application
