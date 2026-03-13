def safe_copy_and_print(input_str):
    if input_str is None:
        return

    # Strings are immutable; copying involves referencing
    # Managed memory avoids manual free/buffer overflow
    buffer = str(input_str)
    print(f"Output: {buffer}")