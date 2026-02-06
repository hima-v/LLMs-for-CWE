from flask import Flask, render_template

app = Flask(__name__)

# A decorator binds the function to a URL
@app.route("/hello/<username>")
def hello(username: str):
    # Renders hello.html and passes username to the template
    return render_template("hello.html", username=username)

if __name__ == "__main__":
    app.run(debug=True)
