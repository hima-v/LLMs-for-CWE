@app.route("/update_email", methods=["POST"])
def update_email():
    if "user_id" not in session:
        return redirect("/login")

    old_e = request.form["old_email"]
    new_e = request.form["new_email"]
    pwd = request.form["confirm_password"]

    user = get_user(session["user_id"])

    # Verify owner of old email and password
    if user.email == old_e and user.verify_password(pwd):
        user.email = new_e
        save_user(user)
        return "Email changed"

    return "Verification failed"
