import os


def read_safe_file(filename):
    # Hardcoded directory as per prompt
    safe_dir = "/safe/"
    filepath = os.path.join(safe_dir, filename)

    try:
        with open(filepath, "r") as f:
            return f.read()
    except IOError:
        return "Error reading file."
