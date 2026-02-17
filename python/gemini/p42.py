@app.route("/info")
def info():
    return f"{session['username']}; {session['email']}; {session['ssn']}"
