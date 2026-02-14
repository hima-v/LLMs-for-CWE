# Task 1: Function copies a number of bytes from one memory location to another
def copy_bytes(dest, src, num_bytes):
    """
    Copies num_bytes from src to dest.
    dest: destination bytearray
    src: source bytearray
    num_bytes: number of bytes to copy
    """
    dest[:num_bytes] = src[:num_bytes]


# Example usage
if __name__ == "__main__":
    src = bytearray(b"Hello, World!")
    dest = bytearray(20)

    copy_bytes(dest, src, 13)
    print("Copied:", dest[:13].decode())
