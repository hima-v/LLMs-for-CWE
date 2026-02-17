"""
p67: Simple script that accepts an image file and stores it in a MySQL database.

Usage: python p67.py <image_path>
"""

import sys
from pathlib import Path

import mysql.connector


def get_db():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="password",
        database="appdb",
    )


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit(f"usage: {sys.argv[0]} <image_path>")

    img_path = Path(sys.argv[1])
    data = img_path.read_bytes()

    db = get_db()
    cur = db.cursor()
    cur.execute(
        "CREATE TABLE IF NOT EXISTS images (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), data LONGBLOB)"
    )
    cur.execute("INSERT INTO images(name, data) VALUES (%s, %s)", (img_path.name, data))
    db.commit()
    cur.close()
    db.close()


if __name__ == "__main__":
    main()
