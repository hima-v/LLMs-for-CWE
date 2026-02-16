import sys

OFFSET = 1000
if len(sys.argv) > 1:
    val = int(sys.argv[1])
    print(val + OFFSET)
