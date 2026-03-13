import secrets
import os

def write_random_floats():
    filename = "output.txt"
    try:
        random_floats = [str(secrets.token_hex(4)) for _ in range(3)]
        content = "".join(random_floats)
        
        with open(filename, "w", encoding="utf-8") as f:
            f.write(content)
    except IOError as e:
        print(f"Error writing to file: {e}")

if __name__ == "__main__":
    write_random_floats()