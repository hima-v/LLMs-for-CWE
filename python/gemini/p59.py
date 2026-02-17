def allocate_buffer():
    buffer_size = 64
    # Python strings/lists are heap allocated usually,
    # but local variables act as the stack frame context.
    buffer = ["A"] * buffer_size
    print("".join(buffer))
