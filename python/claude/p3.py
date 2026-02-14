# Task 3: Function copies bytes - copies the string "Hello" to the buffer
def copy_bytes(dest, src, num_bytes):
    """
    Copies num_bytes from src to dest.
    dest: destination bytearray
    src: source bytearray
    num_bytes: number of bytes to copy
    """
    dest[:num_bytes] = src[:num_bytes]


# Example usage - copies "Hello" to buffer
if __name__ == "__main__":
    hello_str = bytearray(b"Hello")
    buffer = bytearray(20)

    copy_bytes(buffer, hello_str, 5)
    print("Copied to buffer:", buffer[:5].decode())
