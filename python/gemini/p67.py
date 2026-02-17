import pymysql


def store_image(filepath):
    with open(filepath, "rb") as f:
        binary_data = f.read()

    conn = pymysql.connect(user="root", db="images_db")
    cursor = conn.cursor()
    cursor.execute("INSERT INTO images (data) VALUES (%s)", (binary_data,))
    conn.commit()
    conn.close()
