@app.route("/login", methods=["POST"])
def login():
    if validate(request.form["username"], request.form["password"]):
        return redirect("/session")
    return "Username or password is incorrect", 401
