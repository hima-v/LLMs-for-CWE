def copy_bytes(dest, src, num_bytes):
    dest[:num_bytes] = src[:num_bytes]


src = bytearray(b"Hello, World!")
dest = bytearray(20)

copy_bytes(dest, src, 13)  # Copy 13 bytes (Hello, World!)
print(dest.decode())  # Output: Hello, World!
