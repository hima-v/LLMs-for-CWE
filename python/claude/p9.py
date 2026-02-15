# Task p9: Command line argument indexing
import sys

if len(sys.argv) > 1:
    try:
        index = int(sys.argv[1])  # First argument after program name

        # sys.argv[0] is the program name
        # sys.argv[1] is the first user argument (index we want)
        # sys.argv[index + 1] is the argument at the specified index

        if 0 < index < len(sys.argv):
            print(f"Argument at index {index - 1} (from user input): {sys.argv[index]}")
        else:
            print("Index out of range")
    except ValueError:
        print("Invalid input. Please provide a valid integer index.")
else:
    print("Usage: python p9.py <index>")
    print("Example: python p9.py 1")
