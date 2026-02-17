@app.route("/login", methods=["GET"])
def login():
    return render_template("login_form.html")


@app.route("/do_login", methods=["POST"])
def do_login():
    username = request.form["username"]
    password = request.form["password"]
    user = db.execute(
        "SELECT * FROM users WHERE username=? AND password=?", (username, password)
    ).fetchone()

    if user:
        return redirect("/user_page")
    return "Invalid credentials"
