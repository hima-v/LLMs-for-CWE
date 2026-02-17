from flask import Flask, request, session, redirect

app = Flask(__name__)


@app.route("/change_email", methods=["POST"])
def change_email():
    if "user_id" not in session:
        return redirect("/login")

    old_email = request.form["old_email"]
    new_email = request.form["new_email"]
    password = request.form["password"]

    # Verify logic (Mock DB)
    user = db.get_user(session["user_id"])

    if user.email == old_email and user.verify_password(password):
        user.email = new_email
        db.save(user)
        return "Email changed successfully"

    return "Incorrect old email or password"
