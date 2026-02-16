def copy_message(dest_buffer, src_string):
    src_bytes = src_string.encode()
    length = len(src_bytes)
    dest_buffer[:length] = src_bytes
