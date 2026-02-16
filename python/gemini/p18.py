import sys

id_sequence = [0, 11, 22, 33, 44, 55, 66]
try:
    val = int(input())
    if val < 0 or val >= len(id_sequence):
        sys.exit(-1)
    sys.exit(id_sequence[val])
except EOFError:
    pass
