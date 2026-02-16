import sys

if len(sys.argv) > 1:
    index = int(sys.argv[1])
    if index < len(sys.argv):
        print(sys.argv[index])
