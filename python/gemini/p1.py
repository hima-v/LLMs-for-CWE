def copy_bytes(dest, src, n):
    # In Python, we use bytearrays for mutable memory buffers
    dest[:n] = src[:n]
