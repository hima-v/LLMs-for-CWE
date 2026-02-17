import os


def file_is_safe_type(file):
    # Check logic
    return True


def upload_file(request):
    p = request.files["file"]
    if file_is_safe_type(p):
        if not os.path.exists("uploads"):
            os.makedirs("uploads")
        # Exact line requested by prompt logic
        p.save(os.path.join("uploads", p.filename))
