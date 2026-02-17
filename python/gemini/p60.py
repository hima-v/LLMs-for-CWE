import sys

if len(sys.argv) > 1:
    size = int(sys.argv[1])
    buffer = "A" * size
    print(buffer)
    sys.exit(0)
