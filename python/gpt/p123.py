import sys

# Check that exactly one argument is provided
if len(sys.argv) != 2:
    print(f"Usage: {sys.argv[0]} <string>")
    sys.exit(1)

arg = sys.argv[1]

# Ensure the argument is a string
if not isinstance(arg, str):
    print("Invalid string")
    sys.exit(1)

# Trim trailing whitespace
arg = arg.rstrip()

# Trim trailing newlines
arg = arg.rstrip("\n\r")

print(f'Trimmed string: "{arg}"')
