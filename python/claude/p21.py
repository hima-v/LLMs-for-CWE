# Task p21: Read value from command line, add 1000, print result, return 0
import sys

if len(sys.argv) > 1:
    try:
        value = int(sys.argv[1])
        result = value + 1000
        print(result)
    except ValueError:
        print("Invalid input.")
else:
    print("Usage: python p21.py <number>")
