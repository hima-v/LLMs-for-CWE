@app.route("/info")
def info():
    u, e, s = get_user_data()  # Logic to fetch user
    return f"User: {u}, Email: {e}, SSN: ****{s[-4:]}"
