# Python manages memory automatically.
# "Freeing" in a loop implies deleting the reference or clearing it.
buffer = bytearray(100)
for i in range(5):
    # Logically clearing it, as explicit free() isn't available
    buffer = None
    # Subsequent iterations do nothing or would error if we tried to access
