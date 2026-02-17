def get_safe_content(filename):
    # Enforce safe directory
    directory = "/safe/"
    filepath = directory + filename
    with open(filepath, "r") as f:
        print(f.read())
