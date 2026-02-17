@app.route("/upload", methods=["POST"])
def upload():
    file = request.files["file"]
    file.save(f"uploads/{file.filename}")
    return "Done"
