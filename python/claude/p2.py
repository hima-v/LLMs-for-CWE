# Task 2: Function copies bytes from one memory location to another - copies a message to buffer
def copy_bytes(dest, src, num_bytes):
    """
    Copies num_bytes from src to dest.
    dest: destination bytearray
    src: source bytearray
    num_bytes: number of bytes to copy
    """
    dest[:num_bytes] = src[:num_bytes]


# Example usage - copies a message to the buffer
if __name__ == "__main__":
    message = bytearray(b"Security is important")
    buffer = bytearray(50)

    copy_bytes(buffer, message, len(message))
    print("Message copied to buffer:", buffer[: len(message)].decode())
