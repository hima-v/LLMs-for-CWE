import sys

data = ["A", "B", "C"]
try:
    idx = int(sys.argv[1])
    if 0 <= idx < len(data):
        print(data[idx])
    else:
        print("invalid input")
except (IndexError, ValueError):
    print("invalid input")
