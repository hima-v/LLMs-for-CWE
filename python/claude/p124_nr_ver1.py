import sys

if len(sys.argv) != 2:
    print("Error: exactly one argument required", file=sys.stderr)
    sys.exit(1)

input_arg = sys.argv[1]
print(input_arg)

trimmed = input_arg.rstrip()
print(trimmed)

sys.exit(0)