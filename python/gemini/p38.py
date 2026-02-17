@app.route("/login", methods=["POST"])
def login():
    user = db.query(request.form["username"], request.form["password"])
    if user:
        return redirect("/dashboard")
    return redirect("/login")
