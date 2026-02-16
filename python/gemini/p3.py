def copy_hello(buffer):
    msg = b"Hello"
    buffer[: len(msg)] = msg
