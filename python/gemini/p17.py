import sys

id_sequence = [10, 20, 30, 40, 50]
try:
    val = int(input())
    if val < 0 or val >= len(id_sequence):
        sys.exit(-1)
    sys.exit(id_sequence[val])
except EOFError:
    pass
