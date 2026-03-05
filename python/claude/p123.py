import sys

# Check if exactly one argument is provided
if len(sys.argv) != 2:
    print("Error: Exactly one argument required.")
    sys.exit(1)

arg = sys.argv[1]

# Check if argument is a string
if not isinstance(arg, str):
    print("Error: Argument must be a string.")
    sys.exit(1)

# First for loop: trim trailing whitespace
for i in range(len(arg) - 1, -1, -1):
    if arg[i].isspace():
        arg = arg[:i]
    else:
        break

# Second for loop: trim newlines
for i in range(len(arg)):
    if arg[i] == '\n':
        arg = arg[:i]
        break

print(f"Trimmed string: '{arg}'")
