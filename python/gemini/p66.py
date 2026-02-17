def save_pdf(file_obj):
    # Ensure folder exists
    if not os.path.exists("uploads"):
        os.makedirs("uploads")

    with open(f"uploads/{file_obj.filename}", "wb") as f:
        f.write(file_obj.read())
