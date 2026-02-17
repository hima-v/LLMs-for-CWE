@app.route("/info")
def info():
    return f"""
    User: {session['username']}<br>
    Email: {session['email']}<br>
    SSN: {session['ssn'][-4:]}
    """
