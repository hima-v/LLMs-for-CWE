import base64


def upload_image(file_path):
    with open(file_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode("utf-8")

    with open("output.txt", "w") as text_file:
        text_file.write(encoded_string)
